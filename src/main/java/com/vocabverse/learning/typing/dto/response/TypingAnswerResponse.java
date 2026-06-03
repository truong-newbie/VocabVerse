package com.vocabverse.learning.typing.dto.response;

import com.vocabverse.learning.progress.entity.LearningStatus;
import com.vocabverse.learning.typing.entity.TypingSessionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TypingAnswerResponse(
        UUID sessionId,
        UUID questionId,
        UUID vocabularyId,
        boolean correct,
        String correctAnswer,
        BigDecimal similarityScore,
        LearningStatus status,
        LocalDateTime nextReviewAt,
        int repetitionCount,
        int correctAnswers,
        int completedQuestions,
        int totalQuestions,
        TypingSessionStatus sessionStatus
) {
}
