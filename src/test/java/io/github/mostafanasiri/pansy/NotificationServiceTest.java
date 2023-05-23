package io.github.mostafanasiri.pansy;

import io.github.mostafanasiri.pansy.app.common.BaseEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.CommentEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.PostEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.UserEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.notification.CommentNotificationEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.notification.FollowNotificationEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.notification.LikeNotificationEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.notification.NotificationEntity;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.CommentJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.NotificationJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.PostJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.UserJpaRepository;
import io.github.mostafanasiri.pansy.app.domain.mapper.NotificationDomainMapper;
import io.github.mostafanasiri.pansy.app.domain.model.User;
import io.github.mostafanasiri.pansy.app.domain.model.notification.CommentNotification;
import io.github.mostafanasiri.pansy.app.domain.model.notification.FollowNotification;
import io.github.mostafanasiri.pansy.app.domain.model.notification.LikeNotification;
import io.github.mostafanasiri.pansy.app.domain.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest extends BaseServiceTest {
    @Mock
    private NotificationJpaRepository notificationJpaRepository;
    @Mock
    private UserJpaRepository userJpaRepository;
    @Mock
    private CommentJpaRepository commentJpaRepository;
    @Mock
    private PostJpaRepository postJpaRepository;

    @Mock
    private NotificationDomainMapper notificationDomainMapper;

    @InjectMocks
    private NotificationService service;

    @Test
    public void getNotifications_validInput_usesInputsCorrectly() {
        // Arrange
        var page = 0;
        var size = 1;
        var pageRequest = PageRequest.of(page, size);

        // Act
        service.getNotifications(page, size);

        // Assert
        verify(notificationJpaRepository)
                .getNotifications(AUTHENTICATED_USER_ID, pageRequest);
    }

    @Test
    public void getNotifications_successful_marksUnreadNotificationsAsRead() {
        // Arrange
        var page = 0;
        var size = 1;
        var pageRequest = PageRequest.of(page, size);

        List<NotificationEntity> notifications = new ArrayList<>();
        notifications.add(new CommentNotificationEntity());

        when(notificationJpaRepository.getNotifications(AUTHENTICATED_USER_ID, pageRequest))
                .thenReturn(notifications);

        // Act
        service.getNotifications(page, size);

        // Assert
        var notificationIds = notifications.stream().map(BaseEntity::getId).toList();
        verify(notificationJpaRepository)
                .markNotificationsAsRead(notificationIds);
    }

    @Test
    public void addCommentNotification_successful_storesNotificationInDatabase() {
        // Arrange
        var notifierUserId = 1;
        var notifiedUserId = 2;
        var commentId = 10;
        var postId = 20;

        var notification = new CommentNotification(
                new User(notifierUserId),
                new User(notifiedUserId),
                commentId,
                postId
        );

        var notifierUserEntity = new UserEntity("A", null, null);
        when(userJpaRepository.getReferenceById(notifierUserId))
                .thenReturn(notifierUserEntity);

        var notifiedUserEntity = new UserEntity("B", null, null);
        when(userJpaRepository.getReferenceById(notifiedUserId))
                .thenReturn(notifiedUserEntity);

        var postEntity = new PostEntity(null, "caption", null);
        when(postJpaRepository.getReferenceById(postId))
                .thenReturn(postEntity);

        var commentEntity = new CommentEntity(null, null, "comment");
        when(commentJpaRepository.getReferenceById(commentId))
                .thenReturn(commentEntity);

        // Act
        service.addCommentNotification(notification);

        // Assert
        var expectedEntityToSave = new CommentNotificationEntity(
                notifierUserEntity,
                notifiedUserEntity,
                postEntity,
                commentEntity
        );
        verify(notificationJpaRepository)
                .save(expectedEntityToSave);
    }

    @Test
    public void deleteCommentNotification_successful_deletesNotificationFromDatabase() {
        // Arrange
        var commentId = 1;
        var notificationEntity = new CommentNotificationEntity();

        when(notificationJpaRepository.getCommentNotification(commentId))
                .thenReturn(Optional.of(notificationEntity));

        // Act
        service.deleteCommentNotification(commentId);

        // Assert
        verify(notificationJpaRepository)
                .delete(notificationEntity);
    }

    @Test
    public void addLikeNotification_successful_storesNotificationInDatabase() {
        // Arrange
        var notifierUserId = 1;
        var notifiedUserId = 2;
        var postId = 20;

        var notification = new LikeNotification(
                new User(notifierUserId),
                new User(notifiedUserId),
                postId
        );

        var notifierUserEntity = new UserEntity("A", null, null);
        when(userJpaRepository.getReferenceById(notifierUserId))
                .thenReturn(notifierUserEntity);

        var notifiedUserEntity = new UserEntity("B", null, null);
        when(userJpaRepository.getReferenceById(notifiedUserId))
                .thenReturn(notifiedUserEntity);

        var postEntity = new PostEntity(null, "caption", null);
        when(postJpaRepository.getReferenceById(postId))
                .thenReturn(postEntity);

        // Act
        service.addLikeNotification(notification);

        // Assert
        var expectedEntityToSave = new LikeNotificationEntity(
                notifierUserEntity,
                notifiedUserEntity,
                postEntity
        );
        verify(notificationJpaRepository)
                .save(expectedEntityToSave);
    }

    @Test
    public void deleteLikeNotification_successful_deletesNotificationFromDatabase() {
        // Arrange
        var notifierUserId = 1;
        var postId = 2;
        var notificationEntity = new LikeNotificationEntity();

        when(notificationJpaRepository.getLikeNotification(notifierUserId, postId))
                .thenReturn(Optional.of(notificationEntity));

        // Act
        service.deleteLikeNotification(notifierUserId, postId);

        // Assert
        verify(notificationJpaRepository)
                .delete(notificationEntity);
    }

    @Test
    public void addFollowNotification_successful_storesNotificationInDatabase() {
        // Arrange
        var notifierUserId = 1;
        var notifiedUserId = 2;

        var notification = new FollowNotification(
                new User(notifierUserId),
                new User(notifiedUserId)
        );

        var notifierUserEntity = new UserEntity("A", null, null);
        when(userJpaRepository.getReferenceById(notifierUserId))
                .thenReturn(notifierUserEntity);

        var notifiedUserEntity = new UserEntity("B", null, null);
        when(userJpaRepository.getReferenceById(notifiedUserId))
                .thenReturn(notifiedUserEntity);

        // Act
        service.addFollowNotification(notification);

        // Assert
        var expectedEntityToSave = new FollowNotificationEntity(
                notifierUserEntity,
                notifiedUserEntity
        );
        verify(notificationJpaRepository)
                .save(expectedEntityToSave);
    }

    @Test
    public void deleteFollowNotification_successful_deletesNotificationFromDatabase() {
        // Arrange
        var notifierUserId = 1;
        var notifiedUserId = 2;
        var notificationEntity = new FollowNotificationEntity();

        when(notificationJpaRepository.getFollowNotification(notifierUserId, notifiedUserId))
                .thenReturn(Optional.of(notificationEntity));

        // Act
        service.deleteFollowNotification(notifierUserId, notifiedUserId);

        // Assert
        verify(notificationJpaRepository)
                .delete(notificationEntity);
    }
}
