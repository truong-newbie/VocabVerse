package com.vocabverse.auth.controller;

import com.vocabverse.auth.dto.RegisterRequest;
import com.vocabverse.auth.dto.RegisterResponse;
import com.vocabverse.auth.service.AuthService;
import com.vocabverse.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(
                "Register successfully",
                authService.register(request)
        );
    }
}
