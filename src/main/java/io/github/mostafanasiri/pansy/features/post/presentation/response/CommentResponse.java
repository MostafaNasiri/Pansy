package io.github.mostafanasiri.pansy.features.post.presentation.response;

import java.util.Date;

public record CommentResponse(
        int id,
        UserResponse user,
        String text,
        Date createdAt
) {
}
