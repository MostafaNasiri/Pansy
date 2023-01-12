package io.github.mostafanasiri.pansy.features.post.domain.model;

import org.springframework.lang.Nullable;

public record Author(int id, @Nullable String name, @Nullable String avatar) {
    public Author(int id) {
        this(id, null, null);
    }
}
