package io.github.mostafanasiri.pansy.features.notification.domain.model;

import org.springframework.lang.Nullable;

public record User(
        int id,
        @Nullable String name,
        @Nullable String avatarUrl
) {
    public User(int id) {
        this(id, null, null);
    }
}
