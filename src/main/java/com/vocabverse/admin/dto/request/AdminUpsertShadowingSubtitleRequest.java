package com.vocabverse.admin.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminUpsertShadowingSubtitleRequest(
        @NotNull
        @Min(0)
        Integer startTimeMs,

        @NotNull
        @Min(1)
        Integer endTimeMs,

        @NotBlank
        String englishText,

        @Size(max = 5000)
        String vietnameseText,

        @Min(0)
        Integer orderIndex
) {
}
