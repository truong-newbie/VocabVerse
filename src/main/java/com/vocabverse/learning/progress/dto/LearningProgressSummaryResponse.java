package com.vocabverse.learning.progress.dto;

public record LearningProgressSummaryResponse(
        long newCount,
        long learningCount,
        long reviewingCount,
        long masteredCount
) {
}
