package com.vocabverse.dashboard.dto.response;

import com.vocabverse.review.dto.response.ReviewDueItemResponse;
import java.util.List;

public record ReviewDueResponse(
        long dueTodayCount,
        List<ReviewDueItemResponse> items,
        int page,
        int size,
        long totalItems,
        int totalPages
) {
}
