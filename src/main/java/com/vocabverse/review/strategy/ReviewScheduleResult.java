package com.vocabverse.review.strategy;

import com.vocabverse.learning.progress.entity.LearningStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReviewScheduleResult(
        LearningStatus status,
        int repetitionCount,
        BigDecimal easeFactor,
        int lastIntervalDays,
        int lapseCount,
        int reviewCount,
        BigDecimal fsrsDifficulty,
        BigDecimal fsrsStability,
        BigDecimal fsrsRetrievability,
        LocalDateTime nextReviewAt
) {
}
