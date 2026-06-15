package com.vocabverse.shadowing.dto.response;

import com.vocabverse.shadowing.entity.ShadowingLessonStatus;
import com.vocabverse.shadowing.entity.ShadowingLessonSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ShadowingLessonDetailResponse(
        UUID id,
        ShadowingLessonSource sourceType,
        ShadowingLessonStatus status,
        String title,
        String description,
        String videoUrl,
        String mediaUrl,
        String sourceUrl,
        String url,
        String cloudinaryUrl,
        String secureUrl,
        String fileUrl,
        String assetUrl,
        String thumbnailUrl,
        String duration,
        int progress,
        String errorMessage,
        List<ShadowingSubtitleResponse> subtitles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
