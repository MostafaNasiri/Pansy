package io.github.mostafanasiri.pansy.features.user.dto;

public record GetUserResponse(
        int id,
        String fullName,
        String username,
        String bio,
        String avatar
) {
}
