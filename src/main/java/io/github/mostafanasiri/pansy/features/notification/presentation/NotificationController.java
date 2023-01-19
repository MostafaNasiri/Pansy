package io.github.mostafanasiri.pansy.features.notification.presentation;

import io.github.mostafanasiri.pansy.common.ApiResponse;
import io.github.mostafanasiri.pansy.common.BaseController;
import io.github.mostafanasiri.pansy.features.notification.domain.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notification")
@RestController
public class NotificationController extends BaseController {
    @Autowired
    private NotificationService service;

    @GetMapping("/users/me/notifications")
    @Operation(summary = "Returns a list of the authorized user's notifications")
    public ResponseEntity<ApiResponse<Void>> getNotifications() {

        return null;
    }

    @GetMapping("/users/me/notifications/unread/count")
    @Operation(summary = "Returns total number of the authorized user's unread notifications")
    public ResponseEntity<ApiResponse<Integer>> getUnreadNotificationCount() {
        int result = service.getUnreadNotificationsCount(getCurrentUser().getId());
        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, result), HttpStatus.OK);
    }
}
