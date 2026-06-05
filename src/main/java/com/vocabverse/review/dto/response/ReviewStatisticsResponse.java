package com.vocabverse.review.dto.response;

public record ReviewStatisticsResponse(
        long wordsDue,
        long reviewedToday,
        long currentStreak,
        long totalVocabulary
) {
}
