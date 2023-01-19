package io.github.mostafanasiri.pansy.features.notification.domain.model;

import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
public abstract sealed class Notification
        permits LikeNotification, CommentNotification, FollowNotification {
    @Nullable
    protected Integer id;

    @NonNull
    protected NotificationUser notifierUser;

    @Nullable
    protected NotificationUser notifiedUser;

    public Notification(
            int id,
            @NonNull NotificationUser notifierUser,
            @Nullable NotificationUser notifiedUser
    ) {
        this.id = id;
        this.notifierUser = notifierUser;
        this.notifiedUser = notifiedUser;
    }

    public Notification(@NonNull NotificationUser notifierUser, @Nullable NotificationUser notifiedUser) {
        this.notifierUser = notifierUser;
        this.notifiedUser = notifiedUser;
    }
}
