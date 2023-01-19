package io.github.mostafanasiri.pansy.features.notification.domain.model;

import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
public final class CommentNotification extends Notification {
    private int commentId;
    private int postId;
    private String postThumbnailName;

    public CommentNotification(
            int id,
            @NonNull NotificationUser notifierUser,
            @Nullable NotificationUser notifiedUser,
            int commentId,
            int postId,
            @NonNull String postThumbnailName
    ) {
        super(id, notifierUser, notifiedUser);
        this.commentId = commentId;
        this.postId = postId;
        this.postThumbnailName = postThumbnailName;
    }

    public CommentNotification(
            @NonNull NotificationUser notifierUser,
            @Nullable NotificationUser notifiedUser,
            int commentId,
            int postId
    ) {
        super(notifierUser, notifiedUser);
        this.commentId = commentId;
        this.postId = postId;
    }
}
