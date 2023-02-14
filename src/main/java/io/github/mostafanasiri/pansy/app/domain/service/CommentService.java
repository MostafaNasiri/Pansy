package io.github.mostafanasiri.pansy.app.domain.service;

import io.github.mostafanasiri.pansy.app.common.BaseService;
import io.github.mostafanasiri.pansy.app.common.exception.AuthorizationException;
import io.github.mostafanasiri.pansy.app.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.app.common.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.CommentEntity;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.CommentJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.PostJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.UserJpaRepository;
import io.github.mostafanasiri.pansy.app.domain.mapper.PostDomainMapper;
import io.github.mostafanasiri.pansy.app.domain.model.Comment;
import io.github.mostafanasiri.pansy.app.domain.model.User;
import io.github.mostafanasiri.pansy.app.domain.model.notification.CommentNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService extends BaseService {
    @Autowired
    private PostService postService;
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommentJpaRepository commentJpaRepository;
    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private PostJpaRepository postJpaRepository;

    @Autowired
    private PostDomainMapper postDomainMapper;

    public @NonNull List<Comment> getComments(int postId, int page, int size) {
        var post = postService.getPost(postId);

        var pageRequest = PageRequest.of(page, size);
        var entities = commentJpaRepository.getComments(post.getId(), pageRequest);

        return entities.stream()
                .map(postDomainMapper::commentEntityToComment)
                .toList();
    }

    @Transactional
    public @NonNull Comment addComment(int postId, @NonNull Comment comment) {
        var commentator = userJpaRepository.getReferenceById(getAuthenticatedUserId());
        var post = postService.getPost(postId);

        var commentEntity = new CommentEntity(
                commentator,
                postJpaRepository.getReferenceById(post.getId()),
                comment.text()
        );
        commentEntity = commentJpaRepository.save(commentEntity);

        updatePostCommentCount(post.getId());

        // Add new comment notification for the post's author
        var commentNotification = new CommentNotification(
                new User(commentator.getId()),
                new User(post.getUser().id()),
                commentEntity.getId(),
                postId
        );
        notificationService.addCommentNotification(commentNotification);

        return postDomainMapper.commentEntityToComment(commentEntity);
    }

    @Transactional
    public void deleteComment(int postId, int commentId) {
        var commentator = userJpaRepository.getReferenceById(getAuthenticatedUserId());
        var commentEntity = getCommentEntity(commentId);

        if (commentEntity.getUser().getId() != commentator.getId()) {
            throw new AuthorizationException("Comment does not belong to the authenticated user");
        }

        var post = postService.getPost(postId);

        if (commentEntity.getPost().getId() != post.getId()) {
            throw new InvalidInputException("Comment does not belong to this post");
        }

        commentJpaRepository.delete(commentEntity);
        updatePostCommentCount(post.getId());
        notificationService.deleteCommentNotification(commentEntity.getId());
    }

    private void updatePostCommentCount(int postId) {
        int commentCount = commentJpaRepository.getPostCommentCount(postId);
        postService.updatePostCommentCount(postId, commentCount);
    }

    private CommentEntity getCommentEntity(int commentId) {
        return commentJpaRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(Comment.class, commentId));
    }
}
