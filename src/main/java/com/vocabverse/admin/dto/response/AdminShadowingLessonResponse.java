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
        String originalFilename,
        String youtubeUrl,
        String contentType,
        Long fileSize,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
