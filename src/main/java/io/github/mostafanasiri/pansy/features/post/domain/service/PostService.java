package io.github.mostafanasiri.pansy.features.post.domain.service;

import io.github.mostafanasiri.pansy.common.BaseEntity;
import io.github.mostafanasiri.pansy.common.BaseService;
import io.github.mostafanasiri.pansy.common.exception.AuthorizationException;
import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.common.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.features.file.data.FileRepository;
import io.github.mostafanasiri.pansy.features.file.domain.FileService;
import io.github.mostafanasiri.pansy.features.notification.domain.NotificationService;
import io.github.mostafanasiri.pansy.features.post.data.entity.PostEntity;
import io.github.mostafanasiri.pansy.features.post.data.repository.LikeRepository;
import io.github.mostafanasiri.pansy.features.post.data.repository.PostRepository;
import io.github.mostafanasiri.pansy.features.post.domain.ModelMapper;
import io.github.mostafanasiri.pansy.features.post.domain.model.Image;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import io.github.mostafanasiri.pansy.features.post.domain.model.User;
import io.github.mostafanasiri.pansy.features.user.data.entity.jpa.UserEntity;
import io.github.mostafanasiri.pansy.features.user.data.repo.jpa.UserJpaRepository;
import io.github.mostafanasiri.pansy.features.user.domain.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
public class PostService extends BaseService {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private LikeRepository likeRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private FileService fileService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private ModelMapper modelMapper;

    public List<Post> getUserPosts(int userId, int page, int size) {
        var userEntity = getUserEntity(userId);

        var pageRequest = PageRequest.of(page, size);
        var posts = postRepository.getUserPosts(userEntity, pageRequest);

        // Get ids of the posts that the authenticated user has liked
        var postIds = posts.stream().map(BaseEntity::getId).toList();
        var likedPostIds = likeRepository.getLikedPostIds(getAuthenticatedUserId(), postIds);

        return modelMapper.mapUserPosts(userEntity, posts, likedPostIds);
    }

    @Transactional
    public Post createPost(@NonNull Post input) {
        var authenticatedUserEntity = getUserEntity(getAuthenticatedUserId());

        if (input.images().isEmpty()) {
            throw new InvalidInputException("A post must have at least one image");
        }

        var imageFileIds = input.images().stream().map(Image::id).toList();

        // Check images
        fileService.checkIfFilesExist(new HashSet<>(imageFileIds));
        fileService.checkIfFilesAreAlreadyAttachedToAnEntity(imageFileIds);

        // Create post
        var imageFileEntities = imageFileIds.stream().map(id -> fileRepository.getReferenceById(id)).toList();
        var postEntity = new PostEntity(authenticatedUserEntity, input.caption(), imageFileEntities);
        postEntity = postRepository.save(postEntity);

        userService.updateUserPostCount(
                authenticatedUserEntity.getId(),
                authenticatedUserEntity.getPostCount() + 1
        );

        var user = modelMapper.mapFromUserEntity(authenticatedUserEntity);
        return modelMapper.mapFromPostEntity(user, postEntity, false);
    }

    public Post updatePost(@NonNull Post input) {
        var postEntity = getPostEntity(input.id());

        if (postEntity.getUser().getId() != getAuthenticatedUserId()) {
            throw new AuthorizationException("Post does not belong to authenticated user");
        }
        if (input.images().isEmpty()) {
            throw new InvalidInputException("A post must have at least one image");
        }

        // Check images
        var imageFileIds = input.images().stream().map(Image::id).toList();

        fileService.checkIfFilesExist(new HashSet<>(imageFileIds));

        var newlyAddedImageIds = imageFileIds.stream()
                .filter(id -> !postEntity.hasImage(id))
                .toList();
        if (!newlyAddedImageIds.isEmpty()) {
            fileService.checkIfFilesAreAlreadyAttachedToAnEntity(newlyAddedImageIds);
        }

        // Update post
        var imageFileEntities = imageFileIds.stream().map(id -> fileRepository.getReferenceById(id)).toList();
        postEntity.setCaption(input.caption());
        postEntity.setImages(imageFileEntities);

        var isLikedByAuthenticatedUser = likeRepository.findByUserIdAndPostId(
                getAuthenticatedUserId(), postEntity.getId()
        ).isPresent();

        var user = modelMapper.mapFromUserEntity(getAuthenticatedUser());
        return modelMapper.mapFromPostEntity(user, postRepository.save(postEntity), isLikedByAuthenticatedUser);
    }

    @Transactional
    public void deletePost(int postId) {
        var post = getPostEntity(postId);

        if (post.getUser().getId() != getAuthenticatedUserId()) {
            throw new AuthorizationException("Post does not belong to the authenticated user");
        }

        postRepository.delete(post);

        var authenticatedUserEntity = getUserEntity(getAuthenticatedUserId());
        userService.updateUserPostCount(
                authenticatedUserEntity.getId(),
                authenticatedUserEntity.getPostCount() - 1
        );
    }

    private PostEntity getPostEntity(int postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(Post.class, postId));
    }

    private UserEntity getUserEntity(int userId) {
        return userJpaRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));
    }
}
