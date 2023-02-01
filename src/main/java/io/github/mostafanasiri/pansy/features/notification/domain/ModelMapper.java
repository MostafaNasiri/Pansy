package io.github.mostafanasiri.pansy.features.notification.domain;

import io.github.mostafanasiri.pansy.features.notification.data.entity.CommentNotificationEntity;
import io.github.mostafanasiri.pansy.features.notification.data.entity.FollowNotificationEntity;
import io.github.mostafanasiri.pansy.features.notification.data.entity.LikeNotificationEntity;
import io.github.mostafanasiri.pansy.features.notification.data.entity.NotificationEntity;
import io.github.mostafanasiri.pansy.features.notification.domain.model.*;
import io.github.mostafanasiri.pansy.features.user.data.entity.jpa.UserEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("notificationsFeatureModelMapper")
public class ModelMapper {
    public List<Notification> mapFromNotificationEntities(List<NotificationEntity> entities) {
        return entities.stream()
                .map(entity -> {
                    var notifierUser = mapFromUserEntity(entity.getNotifierUser());

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

    private NotificationUser mapFromUserEntity(UserEntity entity) {
        var avatarName = entity.getAvatar() != null ? entity.getAvatar().getName() : null;

        return new NotificationUser(
                entity.getId(),
                entity.getUsername(),
                avatarName
        );
    }
}
