package io.github.mostafanasiri.pansy.features.post.domain.model;

import org.springframework.lang.Nullable;

public record User(int id, @Nullable String name, @Nullable String avatar) {
    public User(int id) {
        this(id, null, null);
    }
}
