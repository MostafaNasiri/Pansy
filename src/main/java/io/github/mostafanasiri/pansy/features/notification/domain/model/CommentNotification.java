package io.github.mostafanasiri.pansy.features.notification.domain.model;

import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
public final class CommentNotification extends Notification {
    private int commentId;

    private int postId;

    @Nullable
    private String postThumbnailUrl;

    public CommentNotification(
            @NonNull NotificationUser notifierUser,
            @NonNull NotificationUser notifiedUser,
            int commentId,
            int postId
    ) {
        super(notifierUser, notifiedUser);
        this.commentId = commentId;
        this.postId = postId;
    }
}
