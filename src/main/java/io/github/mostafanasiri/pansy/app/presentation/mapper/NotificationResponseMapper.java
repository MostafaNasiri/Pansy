package io.github.mostafanasiri.pansy.app.presentation.mapper;

import io.github.mostafanasiri.pansy.app.domain.model.notification.CommentNotification;
import io.github.mostafanasiri.pansy.app.domain.model.notification.FollowNotification;
import io.github.mostafanasiri.pansy.app.domain.model.notification.LikeNotification;
import io.github.mostafanasiri.pansy.app.domain.model.notification.Notification;
import io.github.mostafanasiri.pansy.app.presentation.FileUtils;
import io.github.mostafanasiri.pansy.app.presentation.response.notification.NotificationData;
import io.github.mostafanasiri.pansy.app.presentation.response.notification.NotificationResponse;
import io.github.mostafanasiri.pansy.app.presentation.response.notification.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("notificationsFeatureResponseMapper")
public class NotificationResponseMapper {
    @Autowired
    private FileUtils fileUtils;

    public List<NotificationResponse> mapFromNotificationModels(List<Notification> notifications) {
        return notifications.stream()
                .map(n -> {
                    var id = n.getId();
                    var notifierUserId = n.getNotifierUser().id();
                    var notifierUsername = n.getNotifierUser().username();
                    var notifierUserAvatarUrl = n.getNotifierUser().avatar().name() != null ?
                            fileUtils.createFileUrl(n.getNotifierUser().avatar().name()) : null;

                    NotificationType type = null;
                    NotificationData data = null;

                    if (n instanceof LikeNotification ln) {
                        type = NotificationType.LIKE;
                        data = getLikeNotificationData(ln);
                    } else if (n instanceof CommentNotification cn) {
                        type = NotificationType.COMMENT;
                        data = getCommentNotificationData(cn);
                    } else if (n instanceof FollowNotification) {
                        type = NotificationType.FOLLOW;
                    }

                    return new NotificationResponse(
                            id,
                            type,
                            notifierUserId,
                            notifierUserAvatarUrl,
                            notifierUsername,
                            data
                    );
                })
                .toList();
    }

    private NotificationData getLikeNotificationData(LikeNotification ln) {
        var postId = ln.getPostId();
        var postThumbnailUrl = fileUtils.createFileUrl(ln.getPostThumbnailName());

        return new NotificationData.Like(postId, postThumbnailUrl);
    }

    private NotificationData.Comment getCommentNotificationData(CommentNotification cn) {
        var postId = cn.getPostId();
        var commentId = cn.getCommentId();
        var postThumbnailUrl = fileUtils.createFileUrl(cn.getPostThumbnailName());

        return new NotificationData.Comment(postId, commentId, postThumbnailUrl);
    }
}
