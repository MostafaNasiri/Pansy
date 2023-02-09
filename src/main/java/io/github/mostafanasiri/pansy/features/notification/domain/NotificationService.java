package io.github.mostafanasiri.pansy.features.notification.domain;

import io.github.mostafanasiri.pansy.common.BaseEntity;
import io.github.mostafanasiri.pansy.common.BaseService;
import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.features.notification.data.NotificationJpaRepository;
import io.github.mostafanasiri.pansy.features.notification.data.entity.CommentNotificationEntity;
import io.github.mostafanasiri.pansy.features.notification.data.entity.FollowNotificationEntity;
import io.github.mostafanasiri.pansy.features.notification.data.entity.LikeNotificationEntity;
import io.github.mostafanasiri.pansy.features.notification.domain.model.CommentNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.FollowNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.LikeNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.Notification;
import io.github.mostafanasiri.pansy.features.post.data.entity.jpa.CommentEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.jpa.PostEntity;
import io.github.mostafanasiri.pansy.features.post.data.repository.jpa.CommentJpaRepository;
import io.github.mostafanasiri.pansy.features.post.data.repository.jpa.PostJpaRepository;
import io.github.mostafanasiri.pansy.features.post.domain.model.Comment;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import io.github.mostafanasiri.pansy.features.user.data.repo.jpa.UserJpaRepository;
import io.github.mostafanasiri.pansy.features.user.domain.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService extends BaseService {
    @Autowired
    private UserService userService;
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
    public List<Notification> getNotifications(int page, int size) {
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

    public void addCommentNotification(CommentNotification notification) {
        var notifierUser = userService.getUser(notification.getNotifierUser().id());
        var notifiedUser = userService.getUser(notification.getNotifiedUser().id());

        var post = getPostEntity(notification.getPostId());
        var comment = getCommentEntity(notification.getCommentId());

        var notificationEntity = new CommentNotificationEntity(
                userJpaRepository.getReferenceById(notifierUser.id()),
                userJpaRepository.getReferenceById(notifiedUser.id()),
                post,
                comment
        );
        notificationJpaRepository.save(notificationEntity);
    }

    public void deleteCommentNotification(int commentId) {
        notificationJpaRepository.getCommentNotification(commentId)
                .ifPresent(entity -> notificationJpaRepository.delete(entity));
    }

    public void addLikeNotification(LikeNotification notification) {
        var notifierUser = userService.getUser(notification.getNotifierUser().id());
        var notifiedUser = userService.getUser(notification.getNotifiedUser().id());

        var post = getPostEntity(notification.getPostId());

        var notificationEntity = new LikeNotificationEntity(
                userJpaRepository.getReferenceById(notifierUser.id()),
                userJpaRepository.getReferenceById(notifiedUser.id()),
                post
        );
        notificationJpaRepository.save(notificationEntity);
    }

    public void deleteLikeNotification(int notifierUserId, int postId) {
        notificationJpaRepository.getLikeNotification(notifierUserId, postId)
                .ifPresent(entity -> notificationJpaRepository.delete(entity));
    }

    public void addFollowNotification(FollowNotification notification) {
        var notifierUser = userService.getUser(notification.getNotifierUser().id());
        var notifiedUser = userService.getUser(notification.getNotifiedUser().id());

        var notificationEntity = new FollowNotificationEntity(
                userJpaRepository.getReferenceById(notifierUser.id()),
                userJpaRepository.getReferenceById(notifiedUser.id())
        );
        notificationJpaRepository.save(notificationEntity);
    }

    public void deleteFollowNotification(int notifierUserId, int notifiedUserId) {
        notificationJpaRepository.getFollowNotification(notifierUserId, notifiedUserId)
                .ifPresent(entity -> notificationJpaRepository.delete(entity));
    }

    private PostEntity getPostEntity(int postId) {
        return postJpaRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(Post.class, postId));
    }

    private CommentEntity getCommentEntity(int commentId) {
        return commentJpaRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(Comment.class, commentId));
    }
}
