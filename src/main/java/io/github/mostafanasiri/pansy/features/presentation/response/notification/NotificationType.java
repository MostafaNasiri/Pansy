package io.github.mostafanasiri.pansy.features.presentation.response.notification;

import com.fasterxml.jackson.annotation.JsonValue;

public enum NotificationType {
    LIKE,
    COMMENT,
    FOLLOW;

    @JsonValue
    public String toLowerCase() {
        return toString().toLowerCase();
    }
}
