package io.github.mostafanasiri.pansy.features.post.domain.service;

import io.github.mostafanasiri.pansy.common.BaseService;
import io.github.mostafanasiri.pansy.common.exception.AuthorizationException;
import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.common.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.features.notification.domain.NotificationService;
import io.github.mostafanasiri.pansy.features.notification.domain.model.CommentNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.NotificationUser;
import io.github.mostafanasiri.pansy.features.post.data.entity.CommentEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.PostEntity;
import io.github.mostafanasiri.pansy.features.post.data.repository.CommentRepository;
import io.github.mostafanasiri.pansy.features.post.data.repository.PostRepository;
import io.github.mostafanasiri.pansy.features.post.domain.ModelMapper;
import io.github.mostafanasiri.pansy.features.post.domain.model.Comment;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService extends BaseService {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private ModelMapper modelMapper;

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
        var commentator = getAuthenticatedUser();
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
        var commentator = getAuthenticatedUser();
        var comment = getCommentEntity(commentId);

        if (comment.getUser() != commentator) {
            throw new AuthorizationException("Comment does not belong to the authenticated user");
        }

        var post = getPostEntity(postId);

        if (comment.getPost() != post) {
            throw new InvalidInputException("Comment does not belong to this post");
        }

        commentRepository.delete(comment);

        post.decrementCommentCount();
        postRepository.save(post);

        notificationService.deleteCommentNotification(comment.getId());
    }

    private CommentEntity getCommentEntity(int commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(Comment.class, commentId));
    }

    private PostEntity getPostEntity(int postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(Post.class, postId));
    }
}
