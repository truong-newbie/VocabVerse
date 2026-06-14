package com.vocabverse.review.dto.response;

import com.vocabverse.learning.progress.entity.LearningStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewDueItemResponse(
        UUID vocabularyId,
        String term,
        String meaning,
        String vietnameseMeaning,
        String partOfSpeech,
        String exampleSentence,
        LearningStatus currentLearningStatus,
        LocalDateTime nextReviewAt,
        int repetitionCount,
        String collectionName
) {
}
