package com.vocabverse.roleplay.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendRoleplayMessageRequest(
        @NotBlank
        @Size(max = 2000)
        String message
) {
}
