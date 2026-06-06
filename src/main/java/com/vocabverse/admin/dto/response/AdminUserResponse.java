package com.vocabverse.admin.dto.response;

import com.vocabverse.user.entity.UserRole;
import com.vocabverse.user.entity.UserStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String email,
        String fullName,
        UserRole role,
        UserStatus status,
        LocalDateTime createdAt
) {
}
