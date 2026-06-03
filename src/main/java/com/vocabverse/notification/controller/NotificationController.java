package com.vocabverse.notification.controller;

import com.vocabverse.common.response.ApiResponse;
import com.vocabverse.notification.dto.response.NotificationPageResponse;
import com.vocabverse.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<NotificationPageResponse> getNotifications(
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ApiResponse.success(notificationService.getNotifications(pageable));
    }

    @GetMapping("/history")
    public ApiResponse<NotificationPageResponse> getNotificationHistory(
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ApiResponse.success(notificationService.getNotificationHistory(pageable));
    }
}
