package io.github.mostafanasiri.pansy.features.presentation.response.post;

import io.github.mostafanasiri.pansy.features.presentation.response.user.MinimalUserResponse;

import java.util.Date;
import java.util.List;

public record PostResponse(
        int id,
        MinimalUserResponse user,
        String caption,
        List<String> imageUrls,
        int likeCount,
        int commentCount,
        boolean isLikedByCurrentUser,
        Date createdAt
) {
}
