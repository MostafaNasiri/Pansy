package io.github.mostafanasiri.pansy.features.post.presentation.request;

import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreatePostRequest(
        @Size(min = 1, max = 1000) String caption,
        @Size(min = 1, max = 3) Set<Integer> imageIds
) {
    public CreatePostRequest {
        caption = caption.trim();
    }
}
