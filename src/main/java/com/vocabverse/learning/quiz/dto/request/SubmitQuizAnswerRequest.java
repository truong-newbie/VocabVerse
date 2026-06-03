package com.vocabverse.learning.quiz.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SubmitQuizAnswerRequest(
        @NotBlank
        String answer
) {
}
