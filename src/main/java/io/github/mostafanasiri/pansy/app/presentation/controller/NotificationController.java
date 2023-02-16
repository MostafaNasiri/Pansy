package io.github.mostafanasiri.pansy.app.presentation.controller;

import io.github.mostafanasiri.pansy.app.common.ApiResponse;
import io.github.mostafanasiri.pansy.app.domain.service.NotificationService;
import io.github.mostafanasiri.pansy.app.presentation.mapper.NotificationResponseMapper;
import io.github.mostafanasiri.pansy.app.presentation.response.notification.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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
    private NotificationResponseMapper notificationResponseMapper;

    @GetMapping("/users/me/notifications")
    @Operation(summary = "Returns a list of the authenticated user's notifications")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "50") @Size(min = 1, max = 50) int size
    ) {
        var notifications = service.getNotifications(page, size);
        var result = notificationResponseMapper.mapFromNotificationModels(notifications);

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, result), HttpStatus.OK);
    }

    @GetMapping("/users/me/notifications/unread/count")
    @Operation(summary = "Returns total number of the authenticated user's unread notifications")
    public ResponseEntity<ApiResponse<Integer>> getUnreadNotificationCount() {
        int result = service.getUnreadNotificationsCount();

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, result), HttpStatus.OK);
    }
}
