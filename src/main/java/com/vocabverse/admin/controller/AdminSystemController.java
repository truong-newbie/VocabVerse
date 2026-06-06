package com.vocabverse.admin.controller;

import com.vocabverse.admin.dto.response.AdminSystemHealthResponse;
import com.vocabverse.admin.service.AdminSystemService;
import com.vocabverse.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/system")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSystemController {

    private final AdminSystemService adminSystemService;

    @GetMapping("/health")
    public ApiResponse<AdminSystemHealthResponse> getHealth() {
        return ApiResponse.success(adminSystemService.getHealth());
    }
}
