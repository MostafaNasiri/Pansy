package io.github.mostafanasiri.pansy.features.notification.domain.model;

import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
public final class LikeNotification extends Notification {
    private int postId;

    @Nullable
    private String postThumbnailUrl;

    public LikeNotification(
            @NonNull NotificationUser notifierUser,
            @NonNull NotificationUser notifiedUser,
            int postId
    ) {
        super(notifierUser, notifiedUser);
        this.postId = postId;
    }
}
