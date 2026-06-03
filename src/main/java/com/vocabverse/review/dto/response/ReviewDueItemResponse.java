package com.vocabverse.review.dto.response;

import com.vocabverse.learning.progress.entity.LearningStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewDueItemResponse(
        UUID vocabularyId,
        String word,
        LearningStatus status,
        int repetitionCount,
        LocalDateTime nextReviewAt,
        LocalDateTime lastReviewedAt
) {
}
