package io.github.mostafanasiri.pansy.features.post.domain.service;

import io.github.mostafanasiri.pansy.common.BaseService;
import io.github.mostafanasiri.pansy.common.exception.AuthorizationException;
import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.common.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.features.file.data.FileJpaRepository;
import io.github.mostafanasiri.pansy.features.file.domain.FileService;
import io.github.mostafanasiri.pansy.features.post.data.entity.jpa.FeedEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.jpa.PostEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.redis.PostRedis;
import io.github.mostafanasiri.pansy.features.post.data.repository.jpa.FeedJpaRepository;
import io.github.mostafanasiri.pansy.features.post.data.repository.jpa.LikeJpaRepository;
import io.github.mostafanasiri.pansy.features.post.data.repository.jpa.PostJpaRepository;
import io.github.mostafanasiri.pansy.features.post.data.repository.redis.PostRedisRepository;
import io.github.mostafanasiri.pansy.features.post.domain.PostDomainMapper;
import io.github.mostafanasiri.pansy.features.post.domain.model.Image;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import io.github.mostafanasiri.pansy.features.user.data.repo.jpa.UserJpaRepository;
import io.github.mostafanasiri.pansy.features.user.data.repo.redis.UserRedisRepository;
import io.github.mostafanasiri.pansy.features.user.domain.model.User;
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
    private FileJpaRepository fileJpaRepository;
    @Autowired
    private FeedJpaRepository feedJpaRepository;

    @Autowired
    private UserService userService;
    @Autowired
    private FileService fileService;
    @Autowired
    private FeedService feedService;
    @Autowired
    private LikeService likeService;

    @Autowired
    private PostDomainMapper postDomainMapper;

    public @NonNull Post getPost(int postId) {
        var postRedis = postRedisRepository.findById(postId);
        if (postRedis.isPresent()) {
            logger.info(String.format("getPost - Fetching post %s from Redis", postId));
            return postDomainMapper.postRedisToPost(postRedis.get());
        }

        logger.info(String.format("getPost - Fetching post %s from database", postId));
        var postEntity = getPostEntity(postId);
        var post = postDomainMapper.postEntityToPost(postEntity);

        savePostInRedis(post);

        return post;
    }

    public @NonNull List<Post> getUserFeed(int userId, int page, int size) {
        if (userId != getAuthenticatedUserId()) {
            throw new AuthorizationException("Forbidden action");
        }

        var feed = feedJpaRepository.findById(userId);

        List<Post> result = new ArrayList<>();

        if (feed.isPresent()) {
            var pageRequest = PageRequest.of(page, size);

            var postIds = feed.get()
                    .getItems()
                    .stream()
                    .map(FeedEntity.FeedItem::postId)
                    .skip(pageRequest.getOffset())
                    .limit(size)
                    .toList();

            result = fetchPosts(postIds);

            // Specify the posts that are liked by the authenticated user
            var likedPostIds = likeJpaRepository.getLikedPostIds(getAuthenticatedUserId(), postIds);
            result.forEach(post -> post.setLikedByAuthenticatedUser(likedPostIds.contains(post.getId())));

            // Order posts by creation date (descending)
            result.sort((p1, p2) -> ((-1) * p1.getCreatedAt().compareTo(p2.getCreatedAt())));
        }

        return result;
    }

    private List<Post> fetchPosts(List<Integer> postIds) {
        var redisPosts = new ArrayList<PostRedis>();
        var unCachedPostIds = new ArrayList<Integer>();

        // Find cached and uncached posts
        postIds.forEach(id -> {
            var postRedis = postRedisRepository.findById(id);

            if (postRedis.isPresent()) {
                logger.info(String.format("fetchPostsById - Fetched post %s from Redis", id));
                redisPosts.add(postRedis.get());
            } else {
                logger.info(String.format(
                        "fetchPostsById - Post %s doesn't exist in Redis. Must fetch it from the database",
                        id
                ));
                unCachedPostIds.add(id);
            }
        });

        // Get ids of the posts that the authenticated user has liked
        var likedPostIds = likeJpaRepository.getLikedPostIds(getAuthenticatedUserId(), postIds);

        // Get uncached posts from the database
        List<Post> unCachedPosts = new ArrayList<>();
        if (!unCachedPostIds.isEmpty()) {
            var unCachedPostEntities = postJpaRepository.getPostsById(unCachedPostIds);

            // Map uncached posts to Post models
            unCachedPosts = postDomainMapper.postEntitiesToPosts(unCachedPostEntities);

            // Save uncached posts in Redis
            unCachedPosts.forEach(this::savePostInRedis);
        }

        // Map cached posts to Post models
        var cachedPosts = postDomainMapper.postsRedisToPosts(redisPosts); // TODO: set isLiked

        // Combine all posts
        var result = new ArrayList<Post>();
        result.addAll(unCachedPosts);
        result.addAll(cachedPosts);

        return result;
    }

    public @NonNull List<Post> getUserPosts(int userId, int page, int size) {
        var user = userService.getUser(userId);

        var pageRequest = PageRequest.of(page, size);
        var userPostIds = postJpaRepository.getUserPostIds(user.id(), pageRequest);

        var userPosts = fetchUserPosts(user, userPostIds);

        // Specify the posts that are liked by the authenticated user
        var likedPostIds = likeJpaRepository.getLikedPostIds(getAuthenticatedUserId(), userPostIds);
        userPosts.forEach(post -> post.setLikedByAuthenticatedUser(likedPostIds.contains(post.getId())));

        return userPosts;
    }

    private List<Post> fetchUserPosts(User user, List<Integer> userPostIds) {
        var redisPosts = new ArrayList<PostRedis>();
        var unCachedPostIds = new ArrayList<Integer>();

        // Find cached and uncached posts
        userPostIds.forEach(id -> {
            var postRedis = postRedisRepository.findById(id);

            if (postRedis.isPresent()) {
                logger.info(String.format("fetchUserPosts - Fetched post %s from Redis", id));
                redisPosts.add(postRedis.get());
            } else {
                logger.info(String.format(
                        "fetchUserPosts - Post %s doesn't exist in Redis. Must fetch it from the database",
                        id
                ));
                unCachedPostIds.add(id);
            }
        });

        // Get uncached posts from the database
        List<Post> unCachedPosts = new ArrayList<>();
        if (!unCachedPostIds.isEmpty()) {
            var unCachedPostEntities = postJpaRepository.getPostsByIdWithoutUser(unCachedPostIds);

            // Map uncached posts to Post models
            unCachedPosts = postDomainMapper.postEntitiesToPosts(user, unCachedPostEntities);

            // Save uncached posts in Redis
            unCachedPosts.forEach(this::savePostInRedis);
        }

        // Map cached posts to Post models
        var cachedPosts = postDomainMapper.postsRedisToPosts(redisPosts);

        // Combine all posts
        var result = new ArrayList<Post>();
        result.addAll(unCachedPosts);
        result.addAll(cachedPosts);

        // Order posts by creation date (descending)
        result.sort((p1, p2) -> ((-1) * p1.getCreatedAt().compareTo(p2.getCreatedAt())));

        return result;
    }

    @Transactional
    public @NonNull Post createPost(@NonNull Post input) {
        var authenticatedUser = userService.getUser(getAuthenticatedUserId());

        if (input.getImages().isEmpty()) {
            throw new InvalidInputException("A post must have at least one image");
        }

        var imageFileIds = input.getImages().stream().map(Image::id).toList();

        checkImagesForCreatePost(imageFileIds);

        // Create post
        var imageFileEntities = imageFileIds.stream().map(id -> fileJpaRepository.getReferenceById(id)).toList();
        var postEntity = new PostEntity(
                userJpaRepository.getReferenceById(getAuthenticatedUserId()),
                input.getCaption(),
                imageFileEntities
        );
        postEntity = postJpaRepository.save(postEntity);

        updateAuthenticatedUserPostCount();

        var post = postDomainMapper.postEntityToPost(authenticatedUser, postEntity);
        savePostInRedis(post);

        feedService.addPostToFollowersFeeds(post);

        return post;
    }

    private void checkImagesForCreatePost(List<Integer> imageFileIds) {
        fileService.checkIfFilesExist(new HashSet<>(imageFileIds));
        fileService.checkIfFilesAreAlreadyAttachedToAnEntity(imageFileIds);
    }

    public void updatePostLikeCount(int postId, int likeCount) {
        var postEntity = getPostEntity(postId);
        postEntity.setLikeCount(likeCount);
        postEntity = postJpaRepository.save(postEntity);

        var post = postDomainMapper.postEntityToPost(postEntity);
        savePostInRedis(post);
    }

    public void updatePostCommentCount(int postId, int commentCount) {
        var postEntity = getPostEntity(postId);
        postEntity.setCommentCount(commentCount);
        postEntity = postJpaRepository.save(postEntity);

        var post = postDomainMapper.postEntityToPost(postEntity);
        savePostInRedis(post);
    }

    @Transactional
    public @NonNull Post updatePost(@NonNull Post input) {
        var authenticatedUser = userService.getUser(getAuthenticatedUserId());
        var postEntity = getPostEntity(input.getId());

        if (postEntity.getUser().getId() != getAuthenticatedUserId()) {
            throw new AuthorizationException("Post does not belong to authenticated user");
        }

        if (input.getImages().isEmpty()) {
            throw new InvalidInputException("A post must have at least one image");
        }

        var imageFileIds = input.getImages().stream().map(Image::id).toList();

        checkImagesForUpdatePost(postEntity, imageFileIds);

        // Update post
        var imageFileEntities = imageFileIds.stream().map(id -> fileJpaRepository.getReferenceById(id)).toList();
        postEntity.setCaption(input.getCaption());
        postEntity.setImages(imageFileEntities);


        var post = postDomainMapper.postEntityToPost(
                authenticatedUser,
                postJpaRepository.save(postEntity)
        );

        var likedByAuthenticatedUser = likeService.isPostLikedByUser(postEntity.getId(), getAuthenticatedUserId());
        post.setLikedByAuthenticatedUser(likedByAuthenticatedUser);

        savePostInRedis(post);

        return post;
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
        logger.info(String.format("Saving post %s in Redis", post.getId()));

        // We can't save a post in Redis if the post's author does not exist in Redis.
        // TODO: So one way might be to first get the author from UserService to make sure that it will be saved in Redis if it isn't saved yet
        userRedisRepository.findById(post.getUser().id())
                .ifPresent(userRedis -> {
                    var postRedis = postDomainMapper.postToPostRedis(userRedis, post);
                    postRedisRepository.save(postRedis);
                });
    }

    @Transactional
    public void deletePost(int postId) {
        var post = getPost(postId);

        if (post.getUser().id() != getAuthenticatedUserId()) {
            throw new AuthorizationException("Post does not belong to the authenticated user");
        }

        postJpaRepository.delete(postJpaRepository.getReferenceById(post.getId()));

        updateAuthenticatedUserPostCount();

        // Delete post from redis
        postRedisRepository.findById(postId)
                .ifPresent(p -> postRedisRepository.delete(p));

        feedService.removePostFromFollowersFeeds(post);
    }

    private void updateAuthenticatedUserPostCount() {
        var userEntity = userJpaRepository.getReferenceById(getAuthenticatedUserId());
        var postCount = postJpaRepository.getUserPostCount(userEntity);
        userService.updateUserPostCount(userEntity.getId(), postCount);
    }

    private PostEntity getPostEntity(int postId) {
        return postJpaRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(Post.class, postId));
    }
}
