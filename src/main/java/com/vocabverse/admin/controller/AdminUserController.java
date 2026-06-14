package com.vocabverse.admin.controller;

import com.vocabverse.admin.dto.request.AdminUpdateUserRoleRequest;
import com.vocabverse.admin.dto.request.AdminUpdateUserStatusRequest;
import com.vocabverse.admin.dto.response.AdminPageResponse;
import com.vocabverse.admin.dto.response.AdminUserResponse;
import com.vocabverse.admin.service.AdminUserService;
import com.vocabverse.common.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ApiResponse<AdminPageResponse<AdminUserResponse>> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String q,
            @PageableDefault(page = 0, size = 20) Pageable pageable
    ) {
        return ApiResponse.success(adminUserService.listUsers(resolveKeyword(search, q), pageable));
    }

    @GetMapping("/{userId}")
    public ApiResponse<AdminUserResponse> getUser(@PathVariable UUID userId) {
        return ApiResponse.success(adminUserService.getUser(userId));
    }

    @PutMapping("/{userId}/status")
    public ApiResponse<AdminUserResponse> updateStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody AdminUpdateUserStatusRequest request
    ) {
        return ApiResponse.success(adminUserService.updateStatus(userId, request));
    }

    @PatchMapping("/{userId}/status")
    public ApiResponse<AdminUserResponse> patchStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody AdminUpdateUserStatusRequest request
    ) {
        return ApiResponse.success(adminUserService.updateStatus(userId, request));
    }

    @PutMapping("/{userId}/role")
    public ApiResponse<AdminUserResponse> updateRole(
            @PathVariable UUID userId,
            @Valid @RequestBody AdminUpdateUserRoleRequest request
    ) {
        return ApiResponse.success(adminUserService.updateRole(userId, request));
    }

    @PatchMapping("/{userId}/role")
    public ApiResponse<AdminUserResponse> patchRole(
            @PathVariable UUID userId,
            @Valid @RequestBody AdminUpdateUserRoleRequest request
    ) {
        return ApiResponse.success(adminUserService.updateRole(userId, request));
    }

    private String resolveKeyword(String search, String q) {
        if (search != null && !search.isBlank()) {
            return search.trim();
        }
        if (q != null && !q.isBlank()) {
            return q.trim();
        }
        return null;
    }
}
