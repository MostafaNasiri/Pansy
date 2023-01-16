package io.github.mostafanasiri.pansy.features.user.domain.model;

import org.springframework.lang.Nullable;

public record Image(int id, @Nullable String name) {
    public Image(int id) {
        this(id, null);
    }
}
