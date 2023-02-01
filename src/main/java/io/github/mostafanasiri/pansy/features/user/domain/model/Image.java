package io.github.mostafanasiri.pansy.features.user.domain.model;

import java.io.Serializable;

public record Image(Integer id, String name) implements Serializable {
    public Image(int id) {
        this(id, null);
    }
}
