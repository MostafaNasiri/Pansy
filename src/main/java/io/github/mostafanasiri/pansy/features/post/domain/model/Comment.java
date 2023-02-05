package io.github.mostafanasiri.pansy.features.post.domain.model;

import io.github.mostafanasiri.pansy.features.user.domain.model.User;

import java.util.Date;

public record Comment(
        Integer id,
        User user,
        String text,
        Date createdAt
) {
    public Comment(String text) {
        this(null, null, text, null);
    }
}
