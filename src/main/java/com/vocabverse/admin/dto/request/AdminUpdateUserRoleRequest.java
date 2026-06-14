package com.vocabverse.admin.dto.request;

import com.vocabverse.user.entity.UserRole;
import jakarta.validation.constraints.NotNull;

public record AdminUpdateUserRoleRequest(
        @NotNull
        UserRole role
) {
}
