package com.vocabverse.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCreateYoutubeShadowingLessonRequest(
        @NotBlank
        @Size(max = 500)
        String youtubeUrl
) {
}
