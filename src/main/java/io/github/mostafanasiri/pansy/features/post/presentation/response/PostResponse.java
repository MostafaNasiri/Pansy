package io.github.mostafanasiri.pansy.features.post.presentation.response;

import java.util.List;

public record PostResponse(
        int id,
        PostAuthorResponse author,
        String caption,
        List<String> imageUrls,
        int likesCount
) {
}
