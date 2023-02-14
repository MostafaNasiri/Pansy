package io.github.mostafanasiri.pansy.app.presentation.response;

public record FullUserResponse(
        int id,
        String fullName,
        String username,
        String bio,
        String avatarUrl,
        int postCount,
        int followerCount,
        int followingCount
) {
}
