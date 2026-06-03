package com.vocabverse.learning.typing.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SubmitTypingAnswerRequest(
        @NotBlank
        String answer
) {
}
