package io.github.mostafanasiri.pansy.features.post.domain.model;

import org.springframework.lang.Nullable;

public record Image(int id, @Nullable String url) {
    public Image(int id) {
        this(id, null);
    }
}
