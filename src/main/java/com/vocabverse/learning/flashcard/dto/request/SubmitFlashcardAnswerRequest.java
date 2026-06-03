package com.vocabverse.learning.flashcard.dto.request;

import com.vocabverse.review.entity.ReviewResult;
import jakarta.validation.constraints.NotNull;

public record SubmitFlashcardAnswerRequest(
        @NotNull
        ReviewResult result
) {
}
