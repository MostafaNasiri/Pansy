package io.github.mostafanasiri.pansy.features.notification.domain.model;

import io.github.mostafanasiri.pansy.features.user.domain.model.User;
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
