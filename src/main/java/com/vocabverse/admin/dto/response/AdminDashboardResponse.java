package com.vocabverse.admin.dto.response;

public record AdminDashboardResponse(
        long totalUsers,
        long activeUsers,
        long totalCollections,
        long publicCollections,
        long totalVocabularies,
        long shadowingLessons,
        long todayReviews,
        long todayNewUsers
) {
}
