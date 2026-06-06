package com.vocabverse.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminModerateCollectionRequest(
        @NotBlank
        @Size(max = 50)
        String action,

        @Size(max = 1000)
        String reason
) {
}
