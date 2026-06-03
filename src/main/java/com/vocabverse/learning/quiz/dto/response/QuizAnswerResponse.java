package com.vocabverse.learning.quiz.dto.response;

import com.vocabverse.learning.progress.entity.LearningStatus;
import com.vocabverse.learning.quiz.entity.QuizSessionStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record QuizAnswerResponse(
        UUID sessionId,
        UUID questionId,
        UUID vocabularyId,
        boolean correct,
        String correctAnswer,
        LearningStatus status,
        LocalDateTime nextReviewAt,
        int repetitionCount,
        int correctAnswers,
        int completedQuestions,
        int totalQuestions,
        QuizSessionStatus sessionStatus
) {
}
