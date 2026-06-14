package com.vocabverse.admin.dto.request;

import com.vocabverse.user.entity.UserStatus;
import jakarta.validation.constraints.NotNull;

public record AdminUpdateUserStatusRequest(
        @NotNull
        UserStatus status
) {
}
