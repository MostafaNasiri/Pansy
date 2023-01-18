package io.github.mostafanasiri.pansy.features.notification.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.Nullable;

@Getter
@AllArgsConstructor
public final class CommentNotification extends Notification {
    private int commentId;

    private int postId;

    @Nullable
    private String postThumbnailUrl;
}
