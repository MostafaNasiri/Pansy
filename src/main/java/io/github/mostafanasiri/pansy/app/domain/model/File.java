package io.github.mostafanasiri.pansy.app.domain.model;

public record File(int id, String name) {
    public File(int id) {
        this(id, null);
    }
}
