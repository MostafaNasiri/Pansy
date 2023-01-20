package io.github.mostafanasiri.pansy.features.notification.presentation;

import io.github.mostafanasiri.pansy.common.ApiResponse;
import io.github.mostafanasiri.pansy.features.notification.domain.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Notification")
@RestController
public class NotificationController {
    @Autowired
    private NotificationService service;

    @Autowired
    private ResponseMapper responseMapper;

    @GetMapping("/users/me/notifications")
    @Operation(summary = "Returns a list of the authenticated user's notifications")
    public ResponseEntity<ApiResponse<List>> getNotifications(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "30") @Max(30) int size
    ) {
        var notifications = service.getNotifications(page, size);
        var result = responseMapper.mapFromNotificationModels(notifications);

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, result), HttpStatus.OK);
    }

    @GetMapping("/users/me/notifications/unread/count")
    @Operation(summary = "Returns total number of the authenticated user's unread notifications")
    public ResponseEntity<ApiResponse<Integer>> getUnreadNotificationCount() {
        int result = service.getUnreadNotificationsCount();

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, result), HttpStatus.OK);
    }
}
