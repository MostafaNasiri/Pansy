package io.github.mostafanasiri.pansy.features.post.domain.service;

import io.github.mostafanasiri.pansy.common.BaseService;
import io.github.mostafanasiri.pansy.common.exception.AuthorizationException;
import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.common.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.features.notification.domain.NotificationService;
import io.github.mostafanasiri.pansy.features.notification.domain.model.CommentNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.NotificationUser;
import io.github.mostafanasiri.pansy.features.post.data.entity.jpa.CommentEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.jpa.PostEntity;
import io.github.mostafanasiri.pansy.features.post.data.repository.jpa.CommentJpaRepository;
import io.github.mostafanasiri.pansy.features.post.data.repository.jpa.PostJpaRepository;
import io.github.mostafanasiri.pansy.features.post.domain.DomainMapper;
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
    private CommentJpaRepository commentJpaRepository;
    @Autowired
    private PostJpaRepository postJpaRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private DomainMapper domainMapper;

    public List<Comment> getComments(int postId, int page, int size) {
        var postEntity = getPostEntity(postId);

        var pageRequest = PageRequest.of(page, size);
        var entities = commentJpaRepository.getComments(postEntity, pageRequest);

        return entities.stream()
                .map(domainMapper::commentEntityToComment)
                .toList();
    }

    @Transactional
    public Comment addComment(int postId, @NonNull Comment comment) {
        var commentator = getAuthenticatedUser();
        var post = getPostEntity(postId);

        var commentEntity = new CommentEntity(commentator, post, comment.text());
        commentEntity = commentJpaRepository.save(commentEntity);

        post.incrementCommentCount();
        postJpaRepository.save(post);

        // Add new comment notification for the post's author
        var commentNotification = new CommentNotification(
                new NotificationUser(commentator.getId()),
                new NotificationUser(post.getUser().getId()),
                commentEntity.getId(),
                postId
        );
        notificationService.addCommentNotification(commentNotification);

        return domainMapper.commentEntityToComment(commentEntity);
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

        commentJpaRepository.delete(comment);

        post.decrementCommentCount();
        postJpaRepository.save(post);

        notificationService.deleteCommentNotification(comment.getId());
    }

    private CommentEntity getCommentEntity(int commentId) {
        return commentJpaRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(Comment.class, commentId));
    }

    private PostEntity getPostEntity(int postId) {
        return postJpaRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(Post.class, postId));
    }
}
