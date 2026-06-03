package com.vocabverse.collection.dto.response;

import com.vocabverse.collection.enums.CollectionVisibility;
import java.time.LocalDateTime;
import java.util.UUID;

public record CollectionResponse(
        UUID id,
        UUID ownerId,
        String title,
        String description,
        CollectionVisibility visibility,
        String thumbnailUrl,
        int totalWords,
        boolean featured,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
