package com.vocabverse.learning.typing.dto.response;

import com.vocabverse.learning.typing.entity.TypingSessionSource;
import com.vocabverse.learning.typing.entity.TypingSessionStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record TypingSessionResponse(
        UUID id,
        UUID userId,
        TypingSessionSource source,
        UUID collectionId,
        TypingSessionStatus status,
        int totalQuestions,
        int correctAnswers,
        int completedQuestions,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
