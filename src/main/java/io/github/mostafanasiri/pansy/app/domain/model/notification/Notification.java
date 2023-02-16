package io.github.mostafanasiri.pansy.app.domain.model.notification;

import io.github.mostafanasiri.pansy.app.domain.model.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
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
