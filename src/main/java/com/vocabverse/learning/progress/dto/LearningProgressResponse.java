package com.vocabverse.learning.progress.dto;

import com.vocabverse.learning.progress.entity.LearningStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record LearningProgressResponse(
        UUID id,
        UUID userId,
        UUID vocabularyId,
        String word,
        LearningStatus status,
        int repetitionCount,
        BigDecimal easeFactor,
        LocalDateTime nextReviewAt,
        LocalDateTime lastReviewedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
