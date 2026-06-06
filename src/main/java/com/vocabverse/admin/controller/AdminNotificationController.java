package com.vocabverse.admin.controller;

import com.vocabverse.admin.dto.response.AdminNotificationStatsResponse;
import com.vocabverse.admin.service.AdminNotificationService;
import com.vocabverse.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/notifications")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;

    @GetMapping("/stats")
    public ApiResponse<AdminNotificationStatsResponse> getStats() {
        return ApiResponse.success(adminNotificationService.getStats());
    }
}
