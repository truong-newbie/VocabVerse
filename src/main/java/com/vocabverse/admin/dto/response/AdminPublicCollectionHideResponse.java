package com.vocabverse.admin.dto.response;

import java.util.UUID;

public record AdminPublicCollectionHideResponse(
        UUID id,
        String status,
        String hiddenReason
) {
}
