package io.github.mostafanasiri.pansy.features.feed.data.entity;

public record FeedItem(
        int userId,
        int postId,
        long createdAt
) {
}
