package io.github.mostafanasiri.pansy.features.notification.domain;

import io.github.mostafanasiri.pansy.common.BaseEntity;
import io.github.mostafanasiri.pansy.common.BaseService;
import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.features.notification.data.NotificationRepository;
import io.github.mostafanasiri.pansy.features.notification.data.entity.CommentNotificationEntity;
import io.github.mostafanasiri.pansy.features.notification.data.entity.FollowNotificationEntity;
import io.github.mostafanasiri.pansy.features.notification.data.entity.LikeNotificationEntity;
import io.github.mostafanasiri.pansy.features.notification.domain.model.CommentNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.FollowNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.LikeNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.Notification;
import io.github.mostafanasiri.pansy.features.post.data.entity.CommentEntity;
import io.github.mostafanasiri.pansy.features.post.data.entity.PostEntity;
import io.github.mostafanasiri.pansy.features.post.data.repository.CommentRepository;
import io.github.mostafanasiri.pansy.features.post.data.repository.PostRepository;
import io.github.mostafanasiri.pansy.features.post.domain.model.Comment;
import io.github.mostafanasiri.pansy.features.post.domain.model.Post;
import io.github.mostafanasiri.pansy.features.post.domain.model.User;
import io.github.mostafanasiri.pansy.features.user.data.entity.jpa.UserEntity;
import io.github.mostafanasiri.pansy.features.user.data.repo.jpa.UserJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService extends BaseService {
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public List<Notification> getNotifications(int page, int size) {
        var pageRequest = PageRequest.of(page, size);
        var result = notificationRepository.getNotifications(getAuthenticatedUserId(), pageRequest);

        var notificationIds = result.stream()
                .map(BaseEntity::getId)
                .toList();
        notificationRepository.markNotificationsAsRead(notificationIds);

        return modelMapper.mapFromNotificationEntities(result);
    }

    public int getUnreadNotificationsCount() {
        return notificationRepository.countByNotifiedUserIdAndIsReadIsFalse(getAuthenticatedUserId());
    }

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
        var notifierUser = getUserEntity(notification.getNotifierUser().id());
        var notifiedUser = getUserEntity(notification.getNotifiedUser().id());

        var notificationEntity = new FollowNotificationEntity(
                notifierUser,
                notifiedUser
        );
        notificationRepository.save(notificationEntity);
    }

    public void deleteFollowNotification(int notifierUserId, int notifiedUserId) {
        notificationRepository.getFollowNotification(notifierUserId, notifiedUserId)
                .ifPresent(entity -> notificationRepository.delete(entity));
    }

    private PostEntity getPostEntity(int postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(Post.class, postId));
    }

    private UserEntity getUserEntity(int userId) {
        return userJpaRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(User.class, userId));
    }

    private CommentEntity getCommentEntity(int commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(Comment.class, commentId));
    }
}
