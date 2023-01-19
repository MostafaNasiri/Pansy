package io.github.mostafanasiri.pansy.features.notification.domain.service;

import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.features.notification.data.entity.CommentNotificationEntity;
import io.github.mostafanasiri.pansy.features.notification.data.entity.LikeNotificationEntity;
import io.github.mostafanasiri.pansy.features.notification.data.repository.NotificationRepository;
import io.github.mostafanasiri.pansy.features.notification.domain.model.CommentNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.FollowNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.LikeNotification;
import io.github.mostafanasiri.pansy.features.post.data.entity.CommentEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.PostEntity;
import io.github.mostafanasiri.pansy.features.post.data.repository.CommentRepository;
import io.github.mostafanasiri.pansy.features.post.data.repository.PostRepository;
import io.github.mostafanasiri.pansy.features.post.domain.model.Comment;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import io.github.mostafanasiri.pansy.features.post.domain.model.User;
import io.github.mostafanasiri.pansy.features.user.data.entity.UserEntity;
import io.github.mostafanasiri.pansy.features.user.data.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    public void addCommentNotification(CommentNotification notification) {
        var notifierUser = getUserEntity(notification.getNotifierUser().id());
        var notifiedUser = getUserEntity(notification.getNotifiedUser().id());

        var post = getPostEntity(notification.getPostId());
        var comment = getCommentEntity(notification.getCommentId());

        var notificationEntity = new CommentNotificationEntity(
                notifierUser,
                notifiedUser,
                post,
                comment
        );
        notificationRepository.save(notificationEntity);
    }

    public void deleteCommentNotification(int commentId) {
        notificationRepository.getCommentNotification(commentId)
                .ifPresent(entity -> notificationRepository.delete(entity));
    }

    public void addLikeNotification(LikeNotification notification) {
        var notifierUser = getUserEntity(notification.getNotifierUser().id());
        var notifiedUser = getUserEntity(notification.getNotifiedUser().id());

        var post = getPostEntity(notification.getPostId());

        var notificationEntity = new LikeNotificationEntity(
                notifierUser,
                notifiedUser,
                post
        );
        notificationRepository.save(notificationEntity);
    }

    public void deleteLikeNotification(int notifierUserId, int postId) {
        notificationRepository.getLikeNotification(notifierUserId, postId)
                .ifPresent(entity -> notificationRepository.delete(entity));
    }

    public void addFollowNotification(FollowNotification notification) {

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
