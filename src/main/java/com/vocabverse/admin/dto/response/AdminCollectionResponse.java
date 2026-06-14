package com.vocabverse.admin.dto.response;

import com.vocabverse.collection.enums.CollectionVisibility;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminCollectionResponse(
        UUID id,
        UUID ownerId,
        String ownerEmail,
        String ownerName,
        String title,
        String description,
        CollectionVisibility visibility,
        String status,
        String thumbnailUrl,
        int totalWords,
        int vocabularyCount,
        boolean featured,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
