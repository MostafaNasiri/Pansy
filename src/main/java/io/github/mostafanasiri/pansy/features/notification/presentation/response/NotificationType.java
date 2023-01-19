package io.github.mostafanasiri.pansy.features.notification.presentation.response;

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
