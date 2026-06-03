package com.vocabverse.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NormalizeVocabularyRequest(
        @NotBlank
        @Size(max = 500)
        String rawText
) {
}
