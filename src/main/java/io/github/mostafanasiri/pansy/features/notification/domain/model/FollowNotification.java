package io.github.mostafanasiri.pansy.features.notification.domain.model;

import io.github.mostafanasiri.pansy.features.user.domain.model.User;

public final class FollowNotification extends Notification {
    public FollowNotification(
            int id,
            User notifierUser,
            User notifiedUser
    ) {
        super(id, notifierUser, notifiedUser);
    }

    public FollowNotification(User notifierUser, User notifiedUser) {
        super(notifierUser, notifiedUser);
    }
}
