package com.vocabverse.dashboard.dto.response;

public record LearningStatusStatsResponse(
        long newCount,
        long learningCount,
        long reviewingCount,
        long masteredCount
) {
}
