package io.github.mostafanasiri.pansy.features.notification.presentation;

import io.github.mostafanasiri.pansy.common.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notification")
@RestController
public class NotificationController {
    @GetMapping("/users/{user_id}/notifications")
    public ResponseEntity<ApiResponse<Void>> getNotifications() {

        return null;
    }
}
