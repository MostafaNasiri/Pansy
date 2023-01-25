package io.github.mostafanasiri.pansy.features.user.domain.model;

public record Image(int id, String name) {
    public Image(int id) {
        this(id, null);
    }
}
