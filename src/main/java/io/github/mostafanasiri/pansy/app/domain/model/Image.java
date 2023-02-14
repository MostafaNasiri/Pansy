package io.github.mostafanasiri.pansy.app.domain.model;

import java.io.Serializable;

public record Image(Integer id, String name) implements Serializable {
    public Image(int id) {
        this(id, null);
    }
}
