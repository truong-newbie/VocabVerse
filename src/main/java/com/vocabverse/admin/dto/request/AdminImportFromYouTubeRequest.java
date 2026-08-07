package com.vocabverse.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdminImportFromYouTubeRequest(
        @NotBlank(message = "YouTube URL is required")
        String youtubeUrl,

        String title,

        String description
) {
}
