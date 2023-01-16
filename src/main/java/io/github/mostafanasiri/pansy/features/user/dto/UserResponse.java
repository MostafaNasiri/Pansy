package io.github.mostafanasiri.pansy.features.user.dto;

public record UserResponse(
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
