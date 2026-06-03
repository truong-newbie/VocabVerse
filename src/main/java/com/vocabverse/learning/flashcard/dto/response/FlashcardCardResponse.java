package com.vocabverse.learning.flashcard.dto.response;

import com.vocabverse.review.entity.ReviewResult;
import java.time.LocalDateTime;
import java.util.UUID;

public record FlashcardCardResponse(
        UUID id,
        UUID vocabularyId,
        String word,
        String phonetic,
        String partOfSpeech,
        String meaningVi,
        String meaningEn,
        ReviewResult result,
        LocalDateTime answeredAt
) {
}
