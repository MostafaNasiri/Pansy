package io.github.mostafanasiri.pansy.app.domain.model;

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
