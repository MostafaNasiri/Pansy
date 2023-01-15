package io.github.mostafanasiri.pansy.features.post.domain.model;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

public record Post(
        @Nullable Integer id,
        @NonNull User user,
        @NonNull String caption,
        @NonNull List<Image> images,
        @Nullable Integer likesCount,
        @Nullable Boolean isLikedByCurrentUser
) {
}
