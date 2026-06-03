package com.vocabverse.auth.dto;

import com.vocabverse.user.entity.UserRole;
import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String refreshToken,
        UserInfo user
) {

    public record UserInfo(
            UUID id,
            String email,
            String fullName,
            UserRole role
    ) {
    }
}
