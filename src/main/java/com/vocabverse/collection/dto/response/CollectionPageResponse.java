package com.vocabverse.collection.dto.response;

import java.util.List;

public record CollectionPageResponse(
        List<CollectionResponse> items,
        int page,
        int size,
        long totalItems,
        int totalPages
) {
}
