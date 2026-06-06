package com.vocabverse.admin.dto.response;

import com.vocabverse.shadowing.entity.ShadowingLessonStatus;
import java.util.UUID;

public record AdminShadowingLessonStatusResponse(
        UUID lessonId,
        ShadowingLessonStatus status,
        int progress,
        String message
) {
}
