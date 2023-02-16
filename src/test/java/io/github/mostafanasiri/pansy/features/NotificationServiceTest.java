package io.github.mostafanasiri.pansy.features;

import io.github.mostafanasiri.pansy.app.common.BaseEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.notification.CommentNotificationEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.notification.NotificationEntity;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.CommentJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.NotificationJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.PostJpaRepository;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.UserJpaRepository;
import io.github.mostafanasiri.pansy.app.domain.mapper.NotificationDomainMapper;
import io.github.mostafanasiri.pansy.app.domain.service.NotificationService;
import io.jsonwebtoken.lang.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;

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

        List<NotificationEntity> notifications = Collections.arrayToList(new NotificationEntity[]{
                new CommentNotificationEntity()
        });
        when(notificationJpaRepository.getNotifications(AUTHENTICATED_USER_ID, pageRequest))
                .thenReturn(notifications);

        // Act
        service.getNotifications(page, size);

        // Assert
        var notificationIds = notifications.stream().map(BaseEntity::getId).toList();
        verify(notificationJpaRepository)
                .markNotificationsAsRead(notificationIds);
    }
}
