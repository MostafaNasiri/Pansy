package io.github.mostafanasiri.pansy.app.presentation.response.notification;

public record NotificationResponse(
        int id,
        NotificationType type,
        int notifierUserId,
        String notifierAvatarUrl,
        String notifierUsername,
        NotificationData data
) {

}

