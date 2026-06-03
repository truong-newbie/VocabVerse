package com.vocabverse.roleplay.dto.request;

import com.vocabverse.roleplay.enums.RoleplayDifficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRoleplaySessionRequest(
        @NotBlank
        @Size(max = 100)
        String topic,

        @NotNull
        RoleplayDifficulty difficulty,

        @NotBlank
        @Size(max = 100)
        String persona
) {
}
