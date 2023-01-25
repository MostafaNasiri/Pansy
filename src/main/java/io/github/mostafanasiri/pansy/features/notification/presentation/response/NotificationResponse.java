package io.github.mostafanasiri.pansy.features.notification.presentation.response;

public record NotificationResponse(
        int id,
        NotificationType type,
        int notifierUserId,
        String notifierAvatarUrl,
        String notifierUsername,
        NotificationData data
) {

}

