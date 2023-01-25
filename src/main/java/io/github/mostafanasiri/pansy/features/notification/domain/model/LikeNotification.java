package io.github.mostafanasiri.pansy.features.notification.domain.model;

import lombok.Getter;

@Getter
public final class LikeNotification extends Notification {
    private final int postId;
    private String postThumbnailName;

    public LikeNotification(
            int id,
            NotificationUser notifierUser,
            NotificationUser notifiedUser,
            int postId,
            String postThumbnailName
    ) {
        super(id, notifierUser, notifiedUser);
        this.postId = postId;
        this.postThumbnailName = postThumbnailName;
    }

    public LikeNotification(
            NotificationUser notifierUser,
            NotificationUser notifiedUser,
            int postId
    ) {
        super(notifierUser, notifiedUser);
        this.postId = postId;
    }
}
