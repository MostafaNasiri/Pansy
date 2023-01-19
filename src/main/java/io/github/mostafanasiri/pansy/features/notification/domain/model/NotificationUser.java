package io.github.mostafanasiri.pansy.features.notification.domain.model;

import org.springframework.lang.Nullable;

public record NotificationUser(
        int id,
        @Nullable String username,
        @Nullable String avatarName
) {
    public NotificationUser(int id) {
        this(id, null, null);
    }
}
