package io.github.mostafanasiri.pansy.features.presentation.response.user;

public record MinimalUserResponse(
        int id,
        String username,
        String fullName,
        String avatarUrl
) {
}
