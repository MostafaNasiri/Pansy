package io.github.mostafanasiri.pansy.app.presentation.response;

import java.util.Date;

public record CommentResponse(
        int id,
        MinimalUserResponse user,
        String text,
        Date createdAt
) {
}
