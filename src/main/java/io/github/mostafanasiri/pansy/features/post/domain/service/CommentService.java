package io.github.mostafanasiri.pansy.features.post.domain.service;

import io.github.mostafanasiri.pansy.common.BaseService;
import io.github.mostafanasiri.pansy.common.exception.AuthorizationException;
import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.common.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.features.notification.domain.NotificationService;
import io.github.mostafanasiri.pansy.features.notification.domain.model.CommentNotification;
import io.github.mostafanasiri.pansy.features.post.data.entity.jpa.CommentEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.jpa.PostEntity;
import io.github.mostafanasiri.pansy.features.post.data.repository.jpa.CommentJpaRepository;
import io.github.mostafanasiri.pansy.features.post.data.repository.jpa.PostJpaRepository;
import io.github.mostafanasiri.pansy.features.post.domain.PostDomainMapper;
import io.github.mostafanasiri.pansy.features.post.domain.model.Comment;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import io.github.mostafanasiri.pansy.features.user.data.repo.jpa.UserJpaRepository;
import io.github.mostafanasiri.pansy.features.user.domain.model.User;
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
    private CommentJpaRepository commentJpaRepository;
    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private PostJpaRepository postJpaRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private PostDomainMapper postDomainMapper;

    public List<Comment> getComments(int postId, int page, int size) {
        var postEntity = getPostEntity(postId);

        var pageRequest = PageRequest.of(page, size);
        var entities = commentJpaRepository.getComments(postEntity, pageRequest);

        return entities.stream()
                .map(postDomainMapper::commentEntityToComment)
                .toList();
    }

    @Transactional
    public Comment addComment(int postId, @NonNull Comment comment) {
        var commentator = userJpaRepository.getReferenceById(getAuthenticatedUserId());
        var postEntity = getPostEntity(postId);

        var commentEntity = new CommentEntity(commentator, postEntity, comment.text());
        commentEntity = commentJpaRepository.save(commentEntity);

        updatePostCommentCount(postEntity);

        // Add new comment notification for the post's author
        var commentNotification = new CommentNotification(
                new User(commentator.getId()),
                new User(postEntity.getUser().getId()),
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

        var postEntity = getPostEntity(postId);

        if (commentEntity.getPost().getId() != postEntity.getId()) {
            throw new InvalidInputException("Comment does not belong to this post");
        }

        commentJpaRepository.delete(commentEntity);
        updatePostCommentCount(postEntity);
        notificationService.deleteCommentNotification(commentEntity.getId());
    }

    private void updatePostCommentCount(PostEntity postEntity) {
        int commentCount = commentJpaRepository.getPostCommentCount(postEntity);
        postService.updatePostCommentCount(postEntity.getId(), commentCount);
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
