package io.github.mostafanasiri.pansy.features.notification.domain.model;

import org.springframework.lang.NonNull;

public final class FollowNotification extends Notification {
    public FollowNotification(@NonNull NotificationUser notifierUser, @NonNull NotificationUser notifiedUser) {
        super(notifierUser, notifiedUser);
    }
}
