package io.github.mostafanasiri.pansy.features.post.domain.model;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record Comment(
        @Nullable Integer id,
        @NonNull User user,
        @NonNull String text
) {
    public Comment(@NonNull User user, @NonNull String text) {
        this(null, user, text);
    }
}
