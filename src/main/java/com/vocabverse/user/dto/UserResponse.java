package com.vocabverse.user.dto;

import com.vocabverse.user.entity.UserRole;
import com.vocabverse.user.entity.UserStatus;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String fullName,
        String avatarUrl,
        UserRole role,
        UserStatus status
) {
}
