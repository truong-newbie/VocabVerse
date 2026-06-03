package com.vocabverse.learning.flashcard.dto.response;

import com.vocabverse.learning.flashcard.entity.FlashcardSessionStatus;
import com.vocabverse.learning.progress.entity.LearningStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record FlashcardAnswerResponse(
        UUID sessionId,
        UUID vocabularyId,
        LearningStatus status,
        LocalDateTime nextReviewAt,
        int repetitionCount,
        int completedCards,
        int totalCards,
        FlashcardSessionStatus sessionStatus
) {
}
