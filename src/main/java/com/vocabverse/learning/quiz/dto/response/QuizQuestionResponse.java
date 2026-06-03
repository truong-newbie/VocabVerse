package com.vocabverse.learning.quiz.dto.response;

import com.vocabverse.learning.quiz.entity.QuizQuestionType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record QuizQuestionResponse(
        UUID id,
        UUID vocabularyId,
        QuizQuestionType questionType,
        String questionText,
        List<String> options,
        String userAnswer,
        Boolean correct,
        LocalDateTime answeredAt
) {
}
