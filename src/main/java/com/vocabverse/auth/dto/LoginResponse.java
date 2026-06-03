package com.vocabverse.auth.dto;

import com.vocabverse.user.entity.UserRole;
import com.vocabverse.user.entity.UserStatus;
import java.util.UUID;

public record LoginResponse(
        UUID id,
        String email,
        String fullName,
        UserRole role,
        UserStatus status
) {
}
