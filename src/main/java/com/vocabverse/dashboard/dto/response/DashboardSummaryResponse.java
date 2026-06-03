package com.vocabverse.dashboard.dto.response;

public record DashboardSummaryResponse(
        long totalVocabularies,
        long totalCollections,
        long newCount,
        long learningCount,
        long reviewingCount,
        long masteredCount,
        long dueTodayCount,
        long completedFlashcardSessions,
        long completedQuizSessions,
        long completedTypingSessions
) {
}
