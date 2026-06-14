package com.vocabverse.shadowing.dto.response;

import java.util.UUID;

public record ShadowingSubtitleResponse(
        UUID id,
        int startTimeMs,
        int endTimeMs,
        String englishText,
        String vietnameseText,
        int orderIndex
) {
}
