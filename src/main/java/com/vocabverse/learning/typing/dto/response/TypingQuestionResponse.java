package com.vocabverse.learning.typing.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TypingQuestionResponse(
        UUID id,
        UUID vocabularyId,
        String promptText,
        String userAnswer,
        Boolean correct,
        BigDecimal similarityScore,
        LocalDateTime answeredAt
) {
}
