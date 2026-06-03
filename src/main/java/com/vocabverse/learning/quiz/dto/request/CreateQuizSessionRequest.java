package com.vocabverse.learning.quiz.dto.request;

import com.vocabverse.learning.quiz.entity.QuizSessionSource;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateQuizSessionRequest(
        @NotNull
        QuizSessionSource source,

        UUID collectionId
) {
}
