package io.github.mostafanasiri.pansy.features.notification.domain.model;

import lombok.Getter;

@Getter
public final class CommentNotification extends Notification {
    private int commentId;
    private int postId;
    private String postThumbnailName;

    public CommentNotification(
            int id,
            NotificationUser notifierUser,
            NotificationUser notifiedUser,
            int commentId,
            int postId,
            String postThumbnailName
    ) {
        super(id, notifierUser, notifiedUser);
        this.commentId = commentId;
        this.postId = postId;
        this.postThumbnailName = postThumbnailName;
    }

    public CommentNotification(
            NotificationUser notifierUser,
            NotificationUser notifiedUser,
            int commentId,
            int postId
    ) {
        super(notifierUser, notifiedUser);
        this.commentId = commentId;
        this.postId = postId;
    }
}
