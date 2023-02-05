package io.github.mostafanasiri.pansy.features.notification.domain;

import io.github.mostafanasiri.pansy.features.notification.data.entity.CommentNotificationEntity;
import io.github.mostafanasiri.pansy.features.notification.data.entity.FollowNotificationEntity;
import io.github.mostafanasiri.pansy.features.notification.data.entity.LikeNotificationEntity;
import io.github.mostafanasiri.pansy.features.notification.data.entity.NotificationEntity;
import io.github.mostafanasiri.pansy.features.notification.domain.model.CommentNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.FollowNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.LikeNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.Notification;
import io.github.mostafanasiri.pansy.features.user.domain.UserDomainMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationDomainMapper {
    @Autowired
    private UserDomainMapper userDomainMapper;

    public List<Notification> notificationEntitiesToNotifications(List<NotificationEntity> entities) {
        return entities.stream()
                .map(entity -> {
                    var notifierUser = userDomainMapper.userEntityToUser(entity.getNotifierUser());

                    Notification notification = null;

                    if (entity instanceof LikeNotificationEntity lne) {
                        var postId = lne.getPost().getId();
                        var postThumbnailName = lne.getPost().getImages().get(0).getName();

                        notification = new LikeNotification(
                                entity.getId(),
                                notifierUser,
                                null,
                                postId,
                                postThumbnailName
                        );
                    } else if (entity instanceof CommentNotificationEntity cne) {
                        var postId = cne.getPost().getId();
                        var commentId = cne.getComment().getId();
                        var postThumbnailName = cne.getPost().getImages().get(0).getName();

                        notification = new CommentNotification(
                                entity.getId(),
                                notifierUser,
                                null,
                                commentId,
                                postId,
                                postThumbnailName
                        );
                    } else if (entity instanceof FollowNotificationEntity) {
                        notification = new FollowNotification(entity.getId(), notifierUser, null);
                    }

                    return notification;
                })
                .toList();
    }
}
