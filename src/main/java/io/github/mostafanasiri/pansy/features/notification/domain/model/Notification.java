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

    @NonNull
    protected NotificationUser notifiedUser;

    protected boolean isRead;

    public Notification(@NonNull NotificationUser notifierUser, @NonNull NotificationUser notifiedUser) {
        this.notifierUser = notifierUser;
        this.notifiedUser = notifiedUser;
    }
}
