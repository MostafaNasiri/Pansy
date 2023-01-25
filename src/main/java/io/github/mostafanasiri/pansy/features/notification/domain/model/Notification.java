package io.github.mostafanasiri.pansy.features.notification.domain.model;

import lombok.Getter;

@Getter
public abstract sealed class Notification
        permits LikeNotification, CommentNotification, FollowNotification {
    protected Integer id;
    protected NotificationUser notifierUser;
    protected NotificationUser notifiedUser;

    public Notification(
            int id,
            NotificationUser notifierUser,
            NotificationUser notifiedUser
    ) {
        this.id = id;
        this.notifierUser = notifierUser;
        this.notifiedUser = notifiedUser;
    }

    public Notification(NotificationUser notifierUser, NotificationUser notifiedUser) {
        this.notifierUser = notifierUser;
        this.notifiedUser = notifiedUser;
    }
}
