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
    protected User notifierUser;

    @NonNull
    protected User notifiedUser;

    protected boolean isRead;
}
