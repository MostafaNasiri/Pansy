package io.github.mostafanasiri.pansy.features.notification.domain.model;

public record NotificationUser( // TODO: Use User
                                int id,
                                String username,
                                String avatarName
) {
    public NotificationUser(int id) {
        this(id, null, null);
    }
}
