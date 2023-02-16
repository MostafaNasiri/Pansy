package io.github.mostafanasiri.pansy.app.domain.mapper;

import io.github.mostafanasiri.pansy.app.data.entity.jpa.notification.CommentNotificationEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.notification.FollowNotificationEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.notification.LikeNotificationEntity;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.notification.NotificationEntity;
import io.github.mostafanasiri.pansy.app.domain.model.User;
import io.github.mostafanasiri.pansy.app.domain.model.notification.CommentNotification;
import io.github.mostafanasiri.pansy.app.domain.model.notification.FollowNotification;
import io.github.mostafanasiri.pansy.app.domain.model.notification.LikeNotification;
import io.github.mostafanasiri.pansy.app.domain.model.notification.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationDomainMapper {
    @Autowired
    private UserDomainMapper userDomainMapper;

    private static CommentNotification getCommentNotification(CommentNotificationEntity cne, User notifierUser) {
        var postId = cne.getPost().getId();
        var commentId = cne.getComment().getId();
        var postThumbnailName = cne.getPost().getImages().get(0).getName();

        return new CommentNotification(
                cne.getId(),
                notifierUser,
                null,
                commentId,
                postId,
                postThumbnailName
        );
    }

    private static LikeNotification getLikeNotification(LikeNotificationEntity lne, User notifierUser) {
        var postId = lne.getPost().getId();
        var postThumbnailName = lne.getPost().getImages().get(0).getName();

        return new LikeNotification(
                lne.getId(),
                notifierUser,
                null,
                postId,
                postThumbnailName
        );
    }

    public List<Notification> notificationEntitiesToNotifications(List<NotificationEntity> entities) {
        return entities.stream()
                .map(entity -> {
                    var notifierUser = userDomainMapper.userEntityToUser(entity.getNotifierUser());

                    Notification notification = null;

                    if (entity instanceof LikeNotificationEntity lne) {
                        notification = getLikeNotification(lne, notifierUser);
                    } else if (entity instanceof CommentNotificationEntity cne) {
                        notification = getCommentNotification(cne, notifierUser);
                    } else if (entity instanceof FollowNotificationEntity) {
                        notification = new FollowNotification(entity.getId(), notifierUser, null);
                    }

                    return notification;
                })
                .toList();
    }
}
