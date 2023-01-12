package io.github.mostafanasiri.pansy.features.post.presentation.request;

import jakarta.validation.constraints.Size;

public record AddCommentRequest(
        @Size(min = 1, max = 500) String text
) {
    public AddCommentRequest {
        text = text.trim();
    }
}
