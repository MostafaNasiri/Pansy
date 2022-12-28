package io.github.mostafanasiri.pansy.features.user.dto;

public record UpdateUserResponse(
        int id,
        String fullName,
        String bio,
        String avatarUrl
) {
}
