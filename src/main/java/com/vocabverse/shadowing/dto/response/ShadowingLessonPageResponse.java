package com.vocabverse.shadowing.dto.response;

import java.util.List;

public record ShadowingLessonPageResponse(
        List<ShadowingLessonSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
