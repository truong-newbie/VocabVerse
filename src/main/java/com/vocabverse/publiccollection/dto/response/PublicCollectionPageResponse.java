package com.vocabverse.publiccollection.dto.response;

import java.util.List;

public record PublicCollectionPageResponse(
        List<PublicCollectionResponse> items,
        int page,
        int size,
        long totalItems,
        int totalPages
) {
}
