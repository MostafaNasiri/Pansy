package io.github.mostafanasiri.pansy.app.domain.service;

import io.github.mostafanasiri.pansy.app.common.BaseService;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.FeedEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.LikeEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.PostEntity;
import io.github.mostafanasiri.pansy.app.data.entity.redis.PostRedis;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.*;
import io.github.mostafanasiri.pansy.app.data.repository.redis.PostRedisRepository;
import io.github.mostafanasiri.pansy.app.data.repository.redis.UserRedisRepository;
import io.github.mostafanasiri.pansy.app.domain.exception.AuthorizationException;
import io.github.mostafanasiri.pansy.app.domain.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.app.domain.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.app.domain.mapper.PostDomainMapper;
import io.github.mostafanasiri.pansy.app.domain.model.Image;
import io.github.mostafanasiri.pansy.app.domain.model.Post;
import io.github.mostafanasiri.pansy.app.domain.model.User;
import io.github.mostafanasiri.pansy.app.domain.model.notification.LikeNotification;
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
    private NotificationService notificationService;

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

    public @NonNull List<Post> getAuthenticatedUserFeed(int page, int size) {
        var feed = feedJpaRepository.findById(getAuthenticatedUserId());

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
        var cachedPosts = getCachedPosts(postIds);
        var cachedPostIds = cachedPosts.stream()
                .map(Post::getId)
                .toList();

        var unCachedPosts = getUnCachedPosts(postIds, cachedPostIds);
        if (!unCachedPosts.isEmpty()) {
            savePostsInRedis(unCachedPosts);
        }

        // Combine all posts
        var result = new ArrayList<Post>();
        result.addAll(unCachedPosts);
        result.addAll(cachedPosts);

        return result;
    }

    private List<Post> getUnCachedPosts(List<Integer> postIds, List<Integer> cachedPostIds) {
        var unCachedPostIds = new ArrayList<>(postIds);
        unCachedPostIds.removeAll(cachedPostIds);

        List<Post> unCachedPosts = new ArrayList<>();

        if (!unCachedPostIds.isEmpty()) {
            logger.info(String.format("getUnCachedPosts - Fetching users %s from database", unCachedPostIds));

            var unCachedPostEntities = postJpaRepository.getPostsById(unCachedPostIds);
            unCachedPosts = postDomainMapper.postEntitiesToPosts(unCachedPostEntities);
        }

        return unCachedPosts;
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
        var cachedPosts = getCachedPosts(userPostIds);
        var cachedPostIds = cachedPosts.stream()
                .map(Post::getId)
                .toList();

        var unCachedPosts = getUnCachedUserPosts(user, userPostIds, cachedPostIds);
        if (!unCachedPosts.isEmpty()) {
            savePostsInRedis(unCachedPosts);
        }

        var result = new ArrayList<Post>();
        result.addAll(unCachedPosts);
        result.addAll(cachedPosts);

        // Order posts by creation date (descending)
        result.sort((p1, p2) -> ((-1) * p1.getCreatedAt().compareTo(p2.getCreatedAt())));

        return result;
    }

    private List<Post> getUnCachedUserPosts(User user, List<Integer> postIds, List<Integer> cachedPostIds) {
        var unCachedPostIds = new ArrayList<>(postIds);
        unCachedPostIds.removeAll(cachedPostIds);

        List<Post> unCachedPosts = new ArrayList<>();

        if (!unCachedPostIds.isEmpty()) {
            logger.info(String.format("getUnCachedUserPosts - Fetching users %s from database", unCachedPostIds));

            var unCachedPostEntities = postJpaRepository.getPostsByIdWithoutUser(unCachedPostIds);
            unCachedPosts = postDomainMapper.postEntitiesToPosts(user, unCachedPostEntities);
        }

        return unCachedPosts;
    }

    private List<Post> getCachedPosts(List<Integer> postIds) {
        var cachedPosts = new ArrayList<PostRedis>();

        postRedisRepository.findAllById(postIds)
                .forEach(postRedis -> {
                    logger.info(String.format("getCachedPosts - Fetched post %s from Redis", postRedis.id()));
                    cachedPosts.add(postRedis);
                });

        return postDomainMapper.postsRedisToPosts(cachedPosts);
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

        var likedByAuthenticatedUser = isPostLikedByUser(postEntity.getId(), getAuthenticatedUserId());
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

    public @NonNull List<User> getPostLikers(int postId, int page, int size) {
        var post = getPost(postId);

        var pageRequest = PageRequest.of(page, size);
        var likerUserIds = likeJpaRepository.getLikerUserIds(post.getId(), pageRequest);

        return userService.getUsers(likerUserIds);
    }

    @Transactional
    public void likePost(int postId) {
        var authenticatedUserHasAlreadyLikedThePost = isPostLikedByUser(postId, getAuthenticatedUserId());

        if (!authenticatedUserHasAlreadyLikedThePost) {
            var userEntity = userJpaRepository.getReferenceById(getAuthenticatedUserId());
            var post = getPost(postId);

            var like = new LikeEntity(userEntity, postJpaRepository.getReferenceById(post.getId()));
            likeJpaRepository.save(like);

            updatePostLikeCount(post.getId());

            var notification = new LikeNotification(
                    new User(getAuthenticatedUserId()),
                    new User(post.getUser().id()),
                    postId
            );
            notificationService.addLikeNotification(notification);
        }
    }

    @Transactional
    public void unlikePost(int userId, int postId) {
        if (getAuthenticatedUserId() != userId) {
            throw new AuthorizationException("Forbidden action");
        }

        var post = getPost(postId);

        var like = likeJpaRepository.findByUserIdAndPostId(userId, post.getId());
        var userHasLikedThePost = like.isPresent();

        if (userHasLikedThePost) {
            likeJpaRepository.delete(like.get());
            updatePostLikeCount(post.getId());
            notificationService.deleteLikeNotification(userId, postId);
        }
    }

    public boolean isPostLikedByUser(int postId, int userId) {
        return likeJpaRepository.findByUserIdAndPostId(userId, postId)
                .isPresent();
    }

    private void updatePostLikeCount(int postId) {
        var likeCount = likeJpaRepository.getPostLikeCount(postId);

        var postEntity = getPostEntity(postId);
        postEntity.setLikeCount(likeCount);
        postEntity = postJpaRepository.save(postEntity);

        var post = postDomainMapper.postEntityToPost(postEntity);
        savePostInRedis(post);
    }

    private void savePostsInRedis(List<Post> posts) {
        posts.forEach(this::savePostInRedis);
    }

    private void savePostInRedis(Post post) {
        logger.info(String.format("Saving post %s in Redis", post.getId()));

        var user = userService.getUser(post.getUser().id());

        var postRedis = postDomainMapper.postToPostRedis(user, post);
        postRedisRepository.save(postRedis);
    }

    private PostEntity getPostEntity(int postId) {
        return postJpaRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(Post.class, postId));
    }
}
