package io.github.mostafanasiri.pansy.features.notification.domain.service;

import io.github.mostafanasiri.pansy.features.notification.data.entity.CommentNotificationEntity;
import io.github.mostafanasiri.pansy.features.notification.data.repository.NotificationRepository;
import io.github.mostafanasiri.pansy.features.notification.domain.model.CommentNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.FollowNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.LikeNotification;
import io.github.mostafanasiri.pansy.features.post.data.repository.CommentRepository;
import io.github.mostafanasiri.pansy.features.post.data.repository.PostRepository;
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
        // TODO: Handle EntityNotFound
        var notifierUser = userRepository.getReferenceById(notification.getNotifierUser().id());
        var notifiedUser = userRepository.getReferenceById(notification.getNotifiedUser().id());

        var post = postRepository.getReferenceById(notification.getPostId());
        var comment = commentRepository.getReferenceById(notification.getCommentId());

        var notificationEntity = new CommentNotificationEntity(
                notifierUser,
                notifiedUser,
                post,
                comment
        );

        notificationRepository.save(notificationEntity);
    }

    public void deleteCommentNotification(int commentId) {
        notificationRepository.getCommentNotificationByCommentId(commentId)
                .ifPresent(
                        commentNotificationEntity ->
                                notificationRepository.delete(commentNotificationEntity)
                );
    }

    public void addLikeNotification(LikeNotification notification) {

    }

    public void addFollowNotification(FollowNotification notification) {

    }
}
