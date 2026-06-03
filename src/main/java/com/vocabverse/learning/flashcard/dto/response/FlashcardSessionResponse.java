package com.vocabverse.learning.flashcard.dto.response;

import com.vocabverse.learning.flashcard.entity.FlashcardSessionSource;
import com.vocabverse.learning.flashcard.entity.FlashcardSessionStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record FlashcardSessionResponse(
        UUID id,
        UUID userId,
        FlashcardSessionSource source,
        UUID collectionId,
        FlashcardSessionStatus status,
        int totalCards,
        int completedCards,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
