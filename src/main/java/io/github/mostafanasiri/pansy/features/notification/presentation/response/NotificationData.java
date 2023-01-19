package io.github.mostafanasiri.pansy.features.notification.presentation.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

public sealed class NotificationData permits NotificationData.Like, NotificationData.Comment {
    @Getter
    @AllArgsConstructor
    public static final class Like extends NotificationData {
        private int postId;
        private String postThumbnailUrl;
    }

    @Getter
    @AllArgsConstructor
    public static final class Comment extends NotificationData {
        private int postId;
        private int commentId;
        private String postThumbnailUrl;
    }
}
