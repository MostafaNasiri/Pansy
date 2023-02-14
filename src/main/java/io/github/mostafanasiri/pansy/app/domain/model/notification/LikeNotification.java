package io.github.mostafanasiri.pansy.app.domain.model.notification;

import io.github.mostafanasiri.pansy.app.domain.model.User;
import lombok.Getter;

@Getter
public final class LikeNotification extends Notification {
    private final int postId;
    private String postThumbnailName;

    public LikeNotification(
            int id,
            User notifierUser,
            User notifiedUser,
            int postId,
            String postThumbnailName
    ) {
        super(id, notifierUser, notifiedUser);
        this.postId = postId;
        this.postThumbnailName = postThumbnailName;
    }

    public LikeNotification(
            User notifierUser,
            User notifiedUser,
            int postId
    ) {
        super(notifierUser, notifiedUser);
        this.postId = postId;
    }
}
