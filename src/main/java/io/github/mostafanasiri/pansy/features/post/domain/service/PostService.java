package io.github.mostafanasiri.pansy.features.post.domain.service;

import io.github.mostafanasiri.pansy.common.BaseEntity;
import io.github.mostafanasiri.pansy.common.BaseService;
import io.github.mostafanasiri.pansy.common.exception.AuthorizationException;
import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.common.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.features.file.FileService;
import io.github.mostafanasiri.pansy.features.notification.domain.model.CommentNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.LikeNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.NotificationUser;
import io.github.mostafanasiri.pansy.features.notification.domain.service.NotificationService;
import io.github.mostafanasiri.pansy.features.post.data.entity.CommentEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.LikeEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.PostEntity;
import io.github.mostafanasiri.pansy.features.post.data.repository.CommentRepository;
import io.github.mostafanasiri.pansy.features.post.data.repository.LikeRepository;
import io.github.mostafanasiri.pansy.features.post.data.repository.PostRepository;
import io.github.mostafanasiri.pansy.features.post.domain.ModelMapper;
import io.github.mostafanasiri.pansy.features.post.domain.model.Comment;
import io.github.mostafanasiri.pansy.features.post.domain.model.Image;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import io.github.mostafanasiri.pansy.features.post.domain.model.User;
import io.github.mostafanasiri.pansy.features.user.data.entity.UserEntity;
import io.github.mostafanasiri.pansy.features.user.data.repo.UserRepository;
import io.github.mostafanasiri.pansy.features.user.domain.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService extends BaseService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

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
        var result = postRepository.getUserPosts(userEntity, getAuthenticatedUser(), pageRequest);

        var authenticatedUserId = getAuthenticatedUserId();
        return result.stream()
                .map(pe -> {
                    var isLikedByCurrentUser = !pe.getLikes().isEmpty();
                    return modelMapper.mapFromPostEntity(pe, isLikedByCurrentUser);
                })
                .toList();
    }

    @Transactional
    public Post createPost(@NonNull Post input) {
        var userEntity = getUserEntity(getAuthenticatedUserId());

        var imageFileEntities = fileService.getFiles(
                input.images()
                        .stream()
                        .map(Image::id)
                        .collect(Collectors.toSet())
        );

        // Make sure that files are not already attached to any entities
        var fileIds = imageFileEntities.stream().map(BaseEntity::getId).toList();
        checkIfFilesAreAlreadyAttachedToAnEntity(fileIds);

        var postEntity = new PostEntity(userEntity, input.caption(), imageFileEntities);
        postEntity = postRepository.save(postEntity);

        userEntity.incrementPostCount();
        userRepository.save(userEntity);

        return modelMapper.mapFromPostEntity(postEntity, false);
    }

    public Post updatePost(@NonNull Post input) {
        var postEntity = getPostEntity(input.id());

        if (postEntity.getUser().getId() != getAuthenticatedUserId()) {
            throw new AuthorizationException("Post does not belong to authenticated user.");
        }

        var imageFileEntities = fileService.getFiles(
                input.images()
                        .stream()
                        .map(Image::id)
                        .collect(Collectors.toSet())
        );

        if (input.images().isEmpty()) {
            throw new InvalidInputException("A post must have at least one image.");
        }

        // Check if there are any new images added to the post
        // and make sure that they are not already attached to any entities
        var newImageIds = input.images()
                .stream()
                .map(Image::id)
                .filter(id -> !postEntity.hasImage(id))
                .toList();
        if (!newImageIds.isEmpty()) {
            checkIfFilesAreAlreadyAttachedToAnEntity(newImageIds);
        }

        postEntity.setCaption(input.caption());
        postEntity.setImages(imageFileEntities);

        var isLikedByCurrentUser = likeRepository.existsByPostIdAndUserId(postEntity.getId(), input.user().id());

        return modelMapper.mapFromPostEntity(postRepository.save(postEntity), isLikedByCurrentUser);
    }

    // TODO: Move to FileService
    private void checkIfFilesAreAlreadyAttachedToAnEntity(List<Integer> fileIds) {
        var result = fileService.getFileIdsThatAreAttachedToAnEntity(fileIds);
        if (!result.isEmpty()) {
            throw new InvalidInputException(
                    String.format(
                            "File with id %s is already attached to an entity.",
                            result.get(0)
                    )
            );
        }
    }

    @Transactional
    public void deletePost(int postId) {
        var post = getPostEntity(postId);

        if (post.getUser().getId() != getAuthenticatedUserId()) {
            throw new AuthorizationException("Post does not belong to the authenticated user.");
        }

        postRepository.delete(post);

        var user = getUserEntity(getAuthenticatedUserId());
        user.decrementPostCount();
        userRepository.save(user);
    }

    public List<Comment> getComments(int postId, int page, int size) {
        var postEntity = getPostEntity(postId);

        var pageRequest = PageRequest.of(page, size);
        var entities = commentRepository.getComments(postEntity, pageRequest);

        return entities.stream()
                .map(modelMapper::mapFromCommentEntity)
                .toList();
    }

    @Transactional
    public Comment addComment(int postId, @NonNull Comment comment) {
        var commentator = getUserEntity(getAuthenticatedUserId());
        var post = getPostEntity(postId);

        var commentEntity = new CommentEntity(commentator, post, comment.text());
        commentEntity = commentRepository.save(commentEntity);

        post.incrementCommentCount();
        postRepository.save(post);

        // Add new comment notification for the post's author
        var commentNotification = new CommentNotification(
                new NotificationUser(commentator.getId()),
                new NotificationUser(post.getUser().getId()),
                commentEntity.getId(),
                postId
        );
        notificationService.addCommentNotification(commentNotification);

        return modelMapper.mapFromCommentEntity(commentEntity);
    }

    @Transactional
    public void deleteComment(int postId, int commentId) {
        var commentator = getUserEntity(getAuthenticatedUserId());
        var comment = getCommentEntity(commentId);

        if (comment.getUser() != commentator) {
            throw new AuthorizationException("Comment does not belong to the authenticated user.");
        }

        var post = getPostEntity(postId);

        if (comment.getPost() != post) {
            throw new InvalidInputException("Comment does not belong to this post.");
        }

        commentRepository.delete(comment);

        post.decrementCommentCount();
        postRepository.save(post);

        notificationService.deleteCommentNotification(comment.getId());
    }

    public List<User> getLikes(int postId, int page, int size) {
        var post = getPostEntity(postId);

        var pageRequest = PageRequest.of(page, size);
        var likes = likeRepository.getLikes(post, pageRequest);

        return likes.stream()
                .map(like -> modelMapper.mapFromUserEntity(like.getUser()))
                .toList();
    }

    @Transactional
    public void likePost(int postId) {
        var userHasAlreadyLikedThePost = likeRepository.findByUserIdAndPostId(
                getAuthenticatedUserId(),
                postId
        ).isPresent();

        if (!userHasAlreadyLikedThePost) {
            var user = getUserEntity(getAuthenticatedUserId());
            var post = getPostEntity(postId);

            var like = new LikeEntity(user, post);
            likeRepository.save(like);

            post.incrementLikeCount();
            postRepository.save(post);

            var notification = new LikeNotification(
                    new NotificationUser(getAuthenticatedUserId()),
                    new NotificationUser(post.getUser().getId()),
                    postId
            );
            notificationService.addLikeNotification(notification);
        }
    }

    @Transactional
    public void unlikePost(int userId, int postId) {
        if (getAuthenticatedUserId() != userId) {
            throw new AuthorizationException("Forbidden action.");
        }

        var like = likeRepository.findByUserIdAndPostId(userId, postId);

        var userHasLikedThePost = like.isPresent();

        if (userHasLikedThePost) {
            likeRepository.delete(like.get());

            var post = getPostEntity(postId);
            post.decrementLikeCount();
            postRepository.save(post);

            notificationService.deleteLikeNotification(userId, postId);
        }
    }

    private PostEntity getPostEntity(int postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(Post.class, postId));
    }

    private UserEntity getUserEntity(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));
    }

    private CommentEntity getCommentEntity(int commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(Comment.class, commentId));
    }
}
