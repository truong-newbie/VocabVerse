package com.vocabverse.learning.quiz.dto.response;

import com.vocabverse.learning.quiz.entity.QuizSessionSource;
import com.vocabverse.learning.quiz.entity.QuizSessionStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record QuizSessionResponse(
        UUID id,
        UUID userId,
        QuizSessionSource source,
        UUID collectionId,
        QuizSessionStatus status,
        int totalQuestions,
        int correctAnswers,
        int completedQuestions,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
