package io.github.mostafanasiri.pansy.features.notification.domain.model;

import io.github.mostafanasiri.pansy.features.user.domain.model.User;
import lombok.Getter;

@Getter
public abstract sealed class Notification
        permits LikeNotification, CommentNotification, FollowNotification {
    protected Integer id;
    protected User notifierUser;
    protected User notifiedUser;

    public Notification(
            int id,
            User notifierUser,
            User notifiedUser
    ) {
        this.id = id;
        this.notifierUser = notifierUser;
        this.notifiedUser = notifiedUser;
    }

    public Notification(User notifierUser, User notifiedUser) {
        this.notifierUser = notifierUser;
        this.notifiedUser = notifiedUser;
    }
}
