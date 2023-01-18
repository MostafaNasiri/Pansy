package io.github.mostafanasiri.pansy.features.notification.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.Nullable;

@Getter
@AllArgsConstructor
public final class LikeNotification extends Notification {
    private int postId;

    @Nullable
    private String postThumbnailUrl;
}
