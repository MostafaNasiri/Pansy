package io.github.mostafanasiri.pansy.features.file.domain;

public record File(int id, String name) {
    public File(int id) {
        this(id, null);
    }
}
