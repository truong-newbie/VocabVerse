package com.vocabverse.publiccollection.dto.response;

import com.vocabverse.collection.enums.CollectionVisibility;
import java.time.LocalDateTime;
import java.util.UUID;

public record PublicCollectionResponse(
        UUID id,
        String title,
        String description,
        CollectionVisibility visibility,
        UUID ownerId,
        String ownerName,
        int vocabularyCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
