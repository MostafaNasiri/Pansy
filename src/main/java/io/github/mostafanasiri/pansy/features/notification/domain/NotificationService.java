package io.github.mostafanasiri.pansy.features.notification.domain;

import io.github.mostafanasiri.pansy.common.BaseEntity;
import io.github.mostafanasiri.pansy.common.BaseService;
import io.github.mostafanasiri.pansy.features.notification.data.NotificationJpaRepository;
import io.github.mostafanasiri.pansy.features.notification.data.entity.CommentNotificationEntity;
import io.github.mostafanasiri.pansy.features.notification.data.entity.FollowNotificationEntity;
import io.github.mostafanasiri.pansy.features.notification.data.entity.LikeNotificationEntity;
import io.github.mostafanasiri.pansy.features.notification.domain.model.CommentNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.FollowNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.LikeNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.Notification;
import io.github.mostafanasiri.pansy.features.post.data.repository.jpa.CommentJpaRepository;
import io.github.mostafanasiri.pansy.features.post.data.repository.jpa.PostJpaRepository;
import io.github.mostafanasiri.pansy.features.user.data.repo.jpa.UserJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService extends BaseService {
    @Autowired
    private NotificationJpaRepository notificationJpaRepository;
    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private CommentJpaRepository commentJpaRepository;
    @Autowired
    private PostJpaRepository postJpaRepository;

    @Autowired
    private NotificationDomainMapper notificationDomainMapper;

    @Transactional
    public @NonNull List<Notification> getNotifications(int page, int size) {
        var pageRequest = PageRequest.of(page, size);
        var result = notificationJpaRepository.getNotifications(getAuthenticatedUserId(), pageRequest);

        var notificationIds = result.stream()
                .map(BaseEntity::getId)
                .toList();
        notificationJpaRepository.markNotificationsAsRead(notificationIds);

        return notificationDomainMapper.notificationEntitiesToNotifications(result);
    }

    public int getUnreadNotificationsCount() {
        return notificationJpaRepository.countByNotifiedUserIdAndIsReadIsFalse(getAuthenticatedUserId());
    }

    public void addCommentNotification(@NonNull CommentNotification notification) {
        var notificationEntity = new CommentNotificationEntity(
                userJpaRepository.getReferenceById(notification.getNotifierUser().id()),
                userJpaRepository.getReferenceById(notification.getNotifiedUser().id()),
                postJpaRepository.getReferenceById(notification.getPostId()),
                commentJpaRepository.getReferenceById(notification.getCommentId())
        );
        notificationJpaRepository.save(notificationEntity);
    }

    public void deleteCommentNotification(int commentId) {
        notificationJpaRepository.getCommentNotification(commentId)
                .ifPresent(entity -> notificationJpaRepository.delete(entity));
    }

    public void addLikeNotification(@NonNull LikeNotification notification) {
        var notificationEntity = new LikeNotificationEntity(
                userJpaRepository.getReferenceById(notification.getNotifierUser().id()),
                userJpaRepository.getReferenceById(notification.getNotifiedUser().id()),
                postJpaRepository.getReferenceById(notification.getPostId())
        );
        notificationJpaRepository.save(notificationEntity);
    }

    public void deleteLikeNotification(int notifierUserId, int postId) {
        notificationJpaRepository.getLikeNotification(notifierUserId, postId)
                .ifPresent(entity -> notificationJpaRepository.delete(entity));
    }

    public void addFollowNotification(@NonNull FollowNotification notification) {
        var notificationEntity = new FollowNotificationEntity(
                userJpaRepository.getReferenceById(notification.getNotifierUser().id()),
                userJpaRepository.getReferenceById(notification.getNotifiedUser().id())
        );
        notificationJpaRepository.save(notificationEntity);
    }

    public void deleteFollowNotification(int notifierUserId, int notifiedUserId) {
        notificationJpaRepository.getFollowNotification(notifierUserId, notifiedUserId)
                .ifPresent(entity -> notificationJpaRepository.delete(entity));
    }
}
