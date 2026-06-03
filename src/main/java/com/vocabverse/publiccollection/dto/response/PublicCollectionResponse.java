package com.vocabverse.publiccollection.dto.response;

import com.vocabverse.collection.enums.CollectionVisibility;
import java.time.LocalDateTime;
import java.util.UUID;

public record PublicCollectionResponse(
        UUID id,
        String title,
        String description,
        CollectionVisibility visibility,
        String thumbnailUrl,
        int totalWords,
        boolean featured,
        UUID ownerId,
        String ownerFullName,
        String ownerAvatarUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
