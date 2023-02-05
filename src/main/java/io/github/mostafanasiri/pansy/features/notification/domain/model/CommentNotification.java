package io.github.mostafanasiri.pansy.features.notification.domain.model;

import io.github.mostafanasiri.pansy.features.user.domain.model.User;
import lombok.Getter;

@Getter
public final class CommentNotification extends Notification {
    private final int commentId;
    private final int postId;
    private String postThumbnailName;

    public CommentNotification(
            int id,
            User notifierUser,
            User notifiedUser,
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
            User notifierUser,
            User notifiedUser,
            int commentId,
            int postId
    ) {
        super(notifierUser, notifiedUser);
        this.commentId = commentId;
        this.postId = postId;
    }
}
