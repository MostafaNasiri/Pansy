package io.github.mostafanasiri.pansy.features.notification.presentation;

import io.github.mostafanasiri.pansy.features.file.presentation.FileUtils;
import io.github.mostafanasiri.pansy.features.notification.domain.model.CommentNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.FollowNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.LikeNotification;
import io.github.mostafanasiri.pansy.features.notification.domain.model.Notification;
import io.github.mostafanasiri.pansy.features.notification.presentation.response.NotificationData;
import io.github.mostafanasiri.pansy.features.notification.presentation.response.NotificationResponse;
import io.github.mostafanasiri.pansy.features.notification.presentation.response.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("notificationsFeatureResponseMapper")
public class ResponseMapper {
    @Autowired
    private FileUtils fileUtils;

    public List<NotificationResponse> mapFromNotificationModels(List<Notification> notifications) {
        return notifications.stream()
                .map(n -> {
                    var id = n.getId();
                    var notifierUserId = n.getNotifierUser().id();
                    var notifierUsername = n.getNotifierUser().username();
                    var notifierUserAvatarUrl = n.getNotifierUser().avatarName() != null ?
                            fileUtils.createFileUrl(n.getNotifierUser().avatarName()) : null;

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
