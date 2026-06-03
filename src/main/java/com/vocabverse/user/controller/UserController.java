package com.vocabverse.user.controller;

import com.vocabverse.common.response.ApiResponse;
import com.vocabverse.user.dto.UserResponse;
import com.vocabverse.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser() {
        return ApiResponse.success(userService.getCurrentUser());
    }
}
