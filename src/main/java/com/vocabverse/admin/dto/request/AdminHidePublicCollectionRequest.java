package com.vocabverse.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminHidePublicCollectionRequest(
        @NotBlank
        @Size(max = 1000)
        String reason
) {
}
