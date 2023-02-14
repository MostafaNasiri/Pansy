package io.github.mostafanasiri.pansy.app.domain.model.notification;

import io.github.mostafanasiri.pansy.app.domain.model.User;

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
