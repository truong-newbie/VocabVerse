package com.vocabverse.shadowing.dto.response;

import com.vocabverse.shadowing.entity.ShadowingLessonStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ShadowingLessonDetailResponse(
        UUID id,
        ShadowingLessonStatus status,
        String title,
        String description,
        String videoUrl,
        String thumbnailUrl,
        String duration,
        int progress,
        List<ShadowingSubtitleResponse> subtitles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
