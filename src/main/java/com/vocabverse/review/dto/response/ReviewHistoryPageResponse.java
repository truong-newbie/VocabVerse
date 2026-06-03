package com.vocabverse.review.dto.response;

import java.util.List;

public record ReviewHistoryPageResponse(
        List<ReviewHistoryResponse> items,
        int page,
        int size,
        long totalItems,
        int totalPages
) {
}
