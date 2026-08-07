package com.vocabverse.admin.dto.response;

import com.vocabverse.shadowing.entity.ShadowingLessonSource;
import com.vocabverse.shadowing.entity.ShadowingLessonStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminShadowingLessonResponse(
        UUID id,
        ShadowingLessonSource source,
        ShadowingLessonStatus status,
        String title,
        String description,
        String originalFilename,
        String videoUrl,
        String thumbnailUrl,
        String cloudinaryPublicId,
        String storageProvider,
        String contentType,
        Long fileSize,
        String duration,
        int progress,
        int subtitleCount,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
