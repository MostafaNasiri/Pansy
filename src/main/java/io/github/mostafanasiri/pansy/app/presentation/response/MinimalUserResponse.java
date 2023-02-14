package io.github.mostafanasiri.pansy.app.presentation.response;

public record MinimalUserResponse(
        int id,
        String username,
        String fullName,
        String avatarUrl
) {
}
