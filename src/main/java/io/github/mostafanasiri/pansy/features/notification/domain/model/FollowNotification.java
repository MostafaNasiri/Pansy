package io.github.mostafanasiri.pansy.features.notification.domain.model;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public final class FollowNotification extends Notification {
    public FollowNotification(
            int id,
            @NonNull NotificationUser notifierUser,
            @Nullable NotificationUser notifiedUser
    ) {
        super(id, notifierUser, notifiedUser);
    }

    public FollowNotification(@NonNull NotificationUser notifierUser, @Nullable NotificationUser notifiedUser) {
        super(notifierUser, notifiedUser);
    }
}
