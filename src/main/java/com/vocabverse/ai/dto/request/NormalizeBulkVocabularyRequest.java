package com.vocabverse.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NormalizeBulkVocabularyRequest(
        @NotBlank
        @Size(max = 5000)
        String rawText,

        String provider,

        @Size(max = 500)
        String userApiKey
) {
}
