package io.github.mostafanasiri.pansy.features.post.domain.model;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Date;

public record Comment(
        @Nullable Integer id,
        @Nullable User user,
        @NonNull String text,
        @Nullable Date createdAt
) {
    public Comment(@NonNull String text) {
        this(null, null, text, null);
    }
}
