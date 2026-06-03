package com.vocabverse.learning.progress.dto;

import java.util.List;

public record LearningProgressPageResponse(
        List<LearningProgressResponse> items,
        int page,
        int size,
        long totalItems,
        int totalPages
) {
}
