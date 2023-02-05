package io.github.mostafanasiri.pansy.features.post.domain.service;

import io.github.mostafanasiri.pansy.common.BaseService;
import io.github.mostafanasiri.pansy.common.exception.AuthorizationException;
import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.common.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.features.file.data.FileRepository;
import io.github.mostafanasiri.pansy.features.file.domain.FileService;
import io.github.mostafanasiri.pansy.features.post.data.entity.jpa.PostEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.redis.PostRedis;
import io.github.mostafanasiri.pansy.features.post.data.repository.jpa.LikeJpaRepository;
import io.github.mostafanasiri.pansy.features.post.data.repository.jpa.PostJpaRepository;
import io.github.mostafanasiri.pansy.features.post.data.repository.redis.PostRedisRepository;
import io.github.mostafanasiri.pansy.features.post.domain.DomainMapper;
import io.github.mostafanasiri.pansy.features.post.domain.model.Image;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import io.github.mostafanasiri.pansy.features.post.domain.model.User;
import io.github.mostafanasiri.pansy.features.user.data.entity.jpa.UserEntity;
import io.github.mostafanasiri.pansy.features.user.data.repo.jpa.UserJpaRepository;
import io.github.mostafanasiri.pansy.features.user.data.repo.redis.UserRedisRepository;
import io.github.mostafanasiri.pansy.features.user.domain.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class PostService extends BaseService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    private PostJpaRepository postJpaRepository;
    @Autowired
    private PostRedisRepository postRedisRepository;
    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private UserRedisRepository userRedisRepository;
    @Autowired
    private LikeJpaRepository likeJpaRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private FileService fileService;
    @Autowired
    private DomainMapper domainMapper;

    public List<Post> getUserPosts(int userId, int page, int size) {
        var user = userService.getUser(userId);
        var userEntity = userJpaRepository.getReferenceById(user.id());

        var pageRequest = PageRequest.of(page, size);
        var userPostIds = postJpaRepository.getUserPostIds(userEntity, pageRequest);

        // Get ids of the posts that the authenticated user has liked
        var likedPostIds = likeJpaRepository.getLikedPostIds(getAuthenticatedUserId(), userPostIds);

        return fetchPosts(userEntity, userPostIds, likedPostIds);
    }

    private List<Post> fetchPosts(UserEntity userEntity, List<Integer> userPostIds, List<Integer> likedPostIds) {
        var postsRedis = new ArrayList<PostRedis>();
        var unCachedPostIds = new ArrayList<Integer>();

        // Find cached and uncached posts
        userPostIds.forEach(id -> {
            var postRedis = postRedisRepository.findById(id);

            if (postRedis.isPresent()) {
                logger.info(String.format("getUserPosts - Fetched post %s from Redis", id));
                postsRedis.add(postRedis.get());
            } else {
                logger.info(String.format(
                        "getUserPosts - User %s doesn't exist in Redis. Must fetch it from the database",
                        id
                ));
                unCachedPostIds.add(id);
            }
        });

        // Get uncached posts from the database
        List<Post> unCachedPosts = new ArrayList<>();
        if (!unCachedPostIds.isEmpty()) {
            var unCachedPostEntities = postJpaRepository.getUserPosts(userEntity, unCachedPostIds);

            // Map cached and uncached posts to Post models
            unCachedPosts = domainMapper.postEntitiesToPosts(userEntity, unCachedPostEntities, likedPostIds);

            // Save uncached posts in Redis
            unCachedPosts.forEach(this::savePostInRedis);
        }

        var cachedPosts = domainMapper.postsRedisToPosts(postsRedis, likedPostIds);

        // Combine all posts
        var result = new ArrayList<Post>();
        result.addAll(unCachedPosts);
        result.addAll(cachedPosts);

        // Order posts by creation date (descending)
        result.sort((p1, p2) -> ((-1) * p1.createdAt().compareTo(p2.createdAt())));

        return result;
    }


    @Transactional
    public Post createPost(@NonNull Post input) {
        var authenticatedUserEntity = getUserEntity(getAuthenticatedUserId());

        if (input.images().isEmpty()) {
            throw new InvalidInputException("A post must have at least one image");
        }

        var imageFileIds = input.images().stream().map(Image::id).toList();

        checkImagesForCreatePost(imageFileIds);

        // Create post
        var imageFileEntities = imageFileIds.stream().map(id -> fileRepository.getReferenceById(id)).toList();
        var postEntity = new PostEntity(authenticatedUserEntity, input.caption(), imageFileEntities);
        postEntity = postJpaRepository.save(postEntity);

        userService.updateUserPostCount(
                authenticatedUserEntity.getId(),
                authenticatedUserEntity.getPostCount() + 1
        );

        var user = domainMapper.userEntityToUser(authenticatedUserEntity);
        var post = domainMapper.postEntityToPost(user, postEntity, false);

        savePostInRedis(post);

        return post;
    }

    private void checkImagesForCreatePost(List<Integer> imageFileIds) {
        fileService.checkIfFilesExist(new HashSet<>(imageFileIds));
        fileService.checkIfFilesAreAlreadyAttachedToAnEntity(imageFileIds);
    }

    @Transactional
    public Post updatePost(@NonNull Post input) {
        var postEntity = getPostEntity(input.id());

        validateUpdatePostInput(input, postEntity);

        var imageFileIds = input.images().stream().map(Image::id).toList();

        checkImagesForUpdatePost(postEntity, imageFileIds);

        // Update post
        var imageFileEntities = imageFileIds.stream().map(id -> fileRepository.getReferenceById(id)).toList();
        postEntity.setCaption(input.caption());
        postEntity.setImages(imageFileEntities);

        var isLikedByAuthenticatedUser = likeJpaRepository.findByUserIdAndPostId(
                getAuthenticatedUserId(), postEntity.getId()
        ).isPresent();

        var user = domainMapper.userEntityToUser(getAuthenticatedUser());
        var post = domainMapper.postEntityToPost(user, postJpaRepository.save(postEntity), isLikedByAuthenticatedUser);

        savePostInRedis(post);

        return post;
    }

    private void validateUpdatePostInput(Post input, PostEntity postEntity) {
        if (postEntity.getUser().getId() != getAuthenticatedUserId()) {
            throw new AuthorizationException("Post does not belong to authenticated user");
        }

        if (input.images().isEmpty()) {
            throw new InvalidInputException("A post must have at least one image");
        }
    }

    private void checkImagesForUpdatePost(PostEntity postEntity, List<Integer> imageFileIds) {
        fileService.checkIfFilesExist(new HashSet<>(imageFileIds));

        var newlyAddedImageIds = imageFileIds.stream()
                .filter(id -> !postEntity.hasImage(id))
                .toList();
        if (!newlyAddedImageIds.isEmpty()) {
            fileService.checkIfFilesAreAlreadyAttachedToAnEntity(newlyAddedImageIds);
        }
    }

    private void savePostInRedis(Post post) {
        logger.info(String.format("Saving post %s in Redis", post.id()));

        userRedisRepository.findById(post.user().id())
                .ifPresent(userRedis -> {
                    var postRedis = domainMapper.postToPostRedis(userRedis, post);
                    postRedisRepository.save(postRedis);
                });
    }

    @Transactional
    public void deletePost(int postId) {
        var post = getPostEntity(postId);

        if (post.getUser().getId() != getAuthenticatedUserId()) {
            throw new AuthorizationException("Post does not belong to the authenticated user");
        }

        postJpaRepository.delete(post);

        // Update authenticated user's postCount field
        var authenticatedUserEntity = getAuthenticatedUser();
        userService.updateUserPostCount(
                authenticatedUserEntity.getId(),
                authenticatedUserEntity.getPostCount() - 1
        );

        // Delete post from redis
        postRedisRepository.findById(postId)
                .ifPresent(p -> postRedisRepository.delete(p));
    }

    private PostEntity getPostEntity(int postId) {
        return postJpaRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(Post.class, postId));
    }

    private UserEntity getUserEntity(int userId) {
        return userJpaRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));
    }
}
