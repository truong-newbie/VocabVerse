package com.vocabverse.review.dto.response;

import java.util.List;

public record ReviewDuePageResponse(
        List<ReviewDueItemResponse> items,
        int page,
        int size,
        long totalItems,
        int totalPages
) {
}
