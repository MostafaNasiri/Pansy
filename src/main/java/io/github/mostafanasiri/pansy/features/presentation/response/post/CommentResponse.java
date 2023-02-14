package io.github.mostafanasiri.pansy.features.presentation.response.post;

import io.github.mostafanasiri.pansy.features.presentation.response.user.MinimalUserResponse;

import java.util.Date;

public record CommentResponse(
        int id,
        MinimalUserResponse user,
        String text,
        Date createdAt
) {
}
