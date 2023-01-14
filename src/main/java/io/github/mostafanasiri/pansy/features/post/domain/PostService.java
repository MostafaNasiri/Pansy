package io.github.mostafanasiri.pansy.features.post.domain;

import io.github.mostafanasiri.pansy.common.BaseEntity;
import io.github.mostafanasiri.pansy.common.exception.AuthorizationException;
import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.common.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.features.file.FileService;
import io.github.mostafanasiri.pansy.features.post.data.entity.CommentEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.LikeEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.PostEntity;
import io.github.mostafanasiri.pansy.features.post.data.repository.CommentRepository;
import io.github.mostafanasiri.pansy.features.post.data.repository.LikeRepository;
import io.github.mostafanasiri.pansy.features.post.data.repository.PostRepository;
import io.github.mostafanasiri.pansy.features.post.domain.model.Comment;
import io.github.mostafanasiri.pansy.features.post.domain.model.Image;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import io.github.mostafanasiri.pansy.features.post.domain.model.User;
import io.github.mostafanasiri.pansy.features.user.UserService;
import io.github.mostafanasiri.pansy.features.user.entity.UserEntity;
import io.github.mostafanasiri.pansy.features.user.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {
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

    public List<Comment> getComments(int postId, int page, int size) {
        var postEntity = getPostEntity(postId);

        var pageRequest = PageRequest.of(page, size);
        var entities = commentRepository.getComments(postEntity, pageRequest);

        return entities.stream()
                .map(this::mapFromCommentEntity)
                .toList();
    }

    public Comment addComment(int postId, Comment comment) {
        var user = getUserEntity(comment.user().id());
        var post = getPostEntity(postId);

        var commentEntity = new CommentEntity(user, post, comment.text());
        commentEntity = commentRepository.save(commentEntity);

        return mapFromCommentEntity(commentEntity);
    }

    public void deleteComment(int userId, int postId, int commentId) {
        var user = getUserEntity(userId);
        var post = getPostEntity(postId);
        var comment = getCommentEntity(commentId);

        if (comment.getUser() != user) {
            throw new AuthorizationException("Comment does not belong to this user.");
        }

        if (comment.getPost() != post) {
            throw new InvalidInputException("Comment does not belong to this post.");
        }

        commentRepository.delete(comment);
    }

    public List<User> getLikes(int postId, int page, int size) {
        var post = getPostEntity(postId);

        var pageRequest = PageRequest.of(page, size);
        var likes = likeRepository.getLikes(post, pageRequest);

        return likes.stream()
                .map(like -> mapFromUserEntity(like.getUser()))
                .toList();
    }

    public void likePost(int userId, int postId) {
        var user = getUserEntity(userId);
        var post = getPostEntity(postId);

        var userHasAlreadyLikedThePost = likeRepository.findByUserIdAndPostId(userId, postId).isPresent();
        if (!userHasAlreadyLikedThePost) {
            var like = new LikeEntity(user, post);
            likeRepository.save(like);
        }
    }

    public void unlikePost(int userId, int postId) {
        var likeEntity = likeRepository.findByUserIdAndPostId(userId, postId);

        var userHasLikedThePost = likeEntity.isPresent();
        if (userHasLikedThePost) {
            likeRepository.delete(likeEntity.get());
        }
    }

    public void deletePost(int userId, int postId) {
        var post = getPostEntity(postId);

        if (post.getUser().getId() != userId) {
            throw new AuthorizationException("Post does not belong to this user.");
        }

        postRepository.delete(post);
    }

    public List<Post> getUserPosts(int currentUserId, int userId, int page, int size) {
        var userEntity = getUserEntity(userId);

        var pageRequest = PageRequest.of(page, size);
        var result = postRepository.findByUserOrderByCreatedAtDesc(userEntity, pageRequest);

        return result.stream()
                .map(pe -> {
                    var likesCount = (int) likeRepository.countByPostId(pe.getId());
                    var isLikedByCurrentUser = likeRepository.existsByPostIdAndUserId(pe.getId(), currentUserId);

                    var likeData = new Post.LikeData(likesCount, isLikedByCurrentUser);

                    return mapFromPostEntity(pe, likeData);
                })
                .toList();
    }

    public Post createPost(Post input) {
        var userEntity = getUserEntity(input.user().id());

        var imageFileEntities = fileService.getFiles(
                input.images()
                        .stream()
                        .map(Image::id)
                        .collect(Collectors.toSet())
        );

        // Make sure that files are not already attached to any posts
        var fileIds = imageFileEntities.stream().map(BaseEntity::getId).toList();
        var fileIdsThatAreAlreadyAttachedToAPost = postRepository.getFileIdsThatAreAttachedToAPost(fileIds);
        if (!fileIdsThatAreAlreadyAttachedToAPost.isEmpty()) {
            throw new InvalidInputException(
                    String.format(
                            "File with id %s is already attached to a post.",
                            fileIdsThatAreAlreadyAttachedToAPost.get(0)
                    )
            );
        }

        var postEntity = new PostEntity(userEntity, input.caption(), imageFileEntities);
        postEntity = postRepository.save(postEntity);

        return mapFromPostEntity(postEntity, null);
    }

    public Post updatePost(Post input) {
        var postEntity = getPostEntity(input.id());

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
        var newImageIds = input.images()
                .stream()
                .map(Image::id)
                .filter(id -> !postEntity.hasImage(id))
                .toList();

        if (!newImageIds.isEmpty()) {
            // Make sure that the new files are not already attached to any posts
            var fileIdsThatAreAlreadyAttachedToAPost =
                    postRepository.getFileIdsThatAreAttachedToAPost(newImageIds);
            if (!fileIdsThatAreAlreadyAttachedToAPost.isEmpty()) {
                throw new InvalidInputException(
                        String.format(
                                "File with id %s is already attached to a post.",
                                fileIdsThatAreAlreadyAttachedToAPost.get(0)
                        )
                );
            }
        }

        postEntity.setCaption(input.caption());
        postEntity.setImages(imageFileEntities);

        return mapFromPostEntity(postRepository.save(postEntity), null); // TODO: Pass like data
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

    private Post mapFromPostEntity(PostEntity entity, Post.LikeData likeData) {
        var user = mapFromUserEntity(entity.getUser());

        var images = entity.getImages()
                .stream()
                .map((i) -> new Image(i.getId(), i.getName()))
                .toList();

        return new Post(entity.getId(), user, entity.getCaption(), images, likeData);
    }

    private Comment mapFromCommentEntity(CommentEntity entity) {
        var user = mapFromUserEntity(entity.getUser());

        return new Comment(entity.getId(), user, entity.getText(), entity.getCreatedAt());
    }

    private User mapFromUserEntity(UserEntity entity) {
        var avatarUrl = entity.getAvatar() != null ? entity.getAvatar().getName() : null;

        return new User(
                entity.getId(),
                entity.getFullName(),
                avatarUrl
        );
    }
}
