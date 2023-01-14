package io.github.mostafanasiri.pansy.features.post.domain.model;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

public record Post(
        @Nullable Integer id,
        @NonNull User user,
        @NonNull String caption,
        @NonNull List<Image> images,
        @Nullable LikeData likeData
) {
    public record LikeData(int likesCount, boolean isLikedByCurrentUser) {

    }
}
