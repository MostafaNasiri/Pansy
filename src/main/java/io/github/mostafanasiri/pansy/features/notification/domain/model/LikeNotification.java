package io.github.mostafanasiri.pansy.features.notification.domain.model;

import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
public final class LikeNotification extends Notification {
    private int postId;
    private String postThumbnailName;

    public LikeNotification(
            int id,
            @NonNull NotificationUser notifierUser,
            @Nullable NotificationUser notifiedUser,
            int postId,
            @NonNull String postThumbnailName
    ) {
        super(id, notifierUser, notifiedUser);
        this.postId = postId;
        this.postThumbnailName = postThumbnailName;
    }

    public LikeNotification(
            @NonNull NotificationUser notifierUser,
            @Nullable NotificationUser notifiedUser,
            int postId
    ) {
        super(notifierUser, notifiedUser);
        this.postId = postId;
    }
}
