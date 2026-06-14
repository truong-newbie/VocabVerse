package com.vocabverse.shadowing.dto.response;

import com.vocabverse.shadowing.entity.ShadowingLessonStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ShadowingLessonSummaryResponse(
        UUID id,
        ShadowingLessonStatus status,
        String title,
        String description,
        String videoUrl,
        String thumbnailUrl,
        String duration,
        int subtitleCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
