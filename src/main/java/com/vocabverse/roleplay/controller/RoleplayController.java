package com.vocabverse.roleplay.controller;

import com.vocabverse.common.response.ApiResponse;
import com.vocabverse.roleplay.dto.request.CreateRoleplaySessionRequest;
import com.vocabverse.roleplay.dto.request.SendRoleplayMessageRequest;
import com.vocabverse.roleplay.dto.response.RoleplayMessageResponse;
import com.vocabverse.roleplay.dto.response.RoleplayReportResponse;
import com.vocabverse.roleplay.dto.response.RoleplaySessionPageResponse;
import com.vocabverse.roleplay.dto.response.RoleplaySessionResponse;
import com.vocabverse.roleplay.service.RoleplayService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roleplay/sessions")
public class RoleplayController {

    private final RoleplayService roleplayService;

    @PostMapping
    public ApiResponse<RoleplaySessionResponse> createSession(
            @Valid @RequestBody CreateRoleplaySessionRequest request
    ) {
        return ApiResponse.success(roleplayService.createSession(request));
    }

    @GetMapping("/{sessionId}")
    public ApiResponse<RoleplaySessionResponse> getSession(@PathVariable UUID sessionId) {
        return ApiResponse.success(roleplayService.getSession(sessionId));
    }

    @GetMapping
    public ApiResponse<RoleplaySessionPageResponse> getSessions(
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ApiResponse.success(roleplayService.getSessions(pageable));
    }

    @PostMapping("/{sessionId}/messages")
    public ApiResponse<RoleplayMessageResponse> sendMessage(
            @PathVariable UUID sessionId,
            @Valid @RequestBody SendRoleplayMessageRequest request
    ) {
        return ApiResponse.success(roleplayService.sendMessage(sessionId, request));
    }

    @PostMapping("/{sessionId}/end")
    public ApiResponse<RoleplayReportResponse> endSession(@PathVariable UUID sessionId) {
        return ApiResponse.success(roleplayService.endSession(sessionId));
    }
}
