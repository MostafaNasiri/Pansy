package io.github.mostafanasiri.pansy.app.presentation.request;

import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateEditPostRequest(
        @Size(min = 1, max = 1000) String caption,
        @Size(min = 1, max = 5) Set<Integer> imageIds
) {
    public CreateEditPostRequest {
        caption = caption.trim();
    }
}
