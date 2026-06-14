package com.vocabverse.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NormalizeVocabularyRequest(
        @NotBlank
        @Size(max = 500)
        String rawText,

        String provider,

        @Size(max = 500)
        String userApiKey
) {
    public NormalizeVocabularyRequest(String rawText) {
        this(rawText, null, null);
    }
}
