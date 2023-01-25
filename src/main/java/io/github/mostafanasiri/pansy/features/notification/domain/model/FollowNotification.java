package io.github.mostafanasiri.pansy.features.notification.domain.model;

public final class FollowNotification extends Notification {
    public FollowNotification(
            int id,
            NotificationUser notifierUser,
            NotificationUser notifiedUser
    ) {
        super(id, notifierUser, notifiedUser);
    }

    public FollowNotification(NotificationUser notifierUser, NotificationUser notifiedUser) {
        super(notifierUser, notifiedUser);
    }
}
