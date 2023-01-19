package io.github.mostafanasiri.pansy.features.notification.presentation.response;

import org.springframework.lang.Nullable;

public record NotificationResponse(
        int id,
        NotificationType type,
        int notifierUserId,
        String notifierAvatarUrl,
        String notifierUsername,
        @Nullable NotificationData data
) {

}

