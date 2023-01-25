package io.github.mostafanasiri.pansy.features.post.domain.model;

public record User(int id, String username, String avatar) {
    public User(int id) {
        this(id, null, null);
    }
}
