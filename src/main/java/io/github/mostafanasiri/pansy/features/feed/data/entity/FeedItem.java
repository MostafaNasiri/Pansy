package io.github.mostafanasiri.pansy.features.feed.data.entity;

public record FeedItem(
        int userId,
        int postId,
        long createdAt
) {
    @Override
    public String toString() {
        return String.format("%s-%s-%s", userId, postId, createdAt);
    }
}
