package io.github.mostafanasiri.pansy.features.notification.domain.model;

public record NotificationUser(
        int id,
        String username,
        String avatarName
) {
    public NotificationUser(int id) {
        this(id, null, null);
    }
}
