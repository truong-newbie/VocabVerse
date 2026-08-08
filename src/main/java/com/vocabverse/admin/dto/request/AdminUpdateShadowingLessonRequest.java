package com.vocabverse.admin.dto.request;

import jakarta.validation.constraints.Size;

public record AdminUpdateShadowingLessonRequest(
        @Size(max = 200)
        String title,

        @Size(max = 5000)
        String description
) {
}
