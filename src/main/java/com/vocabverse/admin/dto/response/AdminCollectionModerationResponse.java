package com.vocabverse.admin.dto.response;

import com.vocabverse.collection.enums.CollectionVisibility;
import java.util.UUID;

public record AdminCollectionModerationResponse(
        UUID id,
        String moderationStatus,
        CollectionVisibility visibility
) {
}
