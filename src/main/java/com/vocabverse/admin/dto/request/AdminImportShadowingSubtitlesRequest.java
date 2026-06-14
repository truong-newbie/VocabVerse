package com.vocabverse.admin.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AdminImportShadowingSubtitlesRequest(
        Boolean replaceExisting,

        @Valid
        @NotEmpty
        @Size(max = 500)
        List<AdminUpsertShadowingSubtitleRequest> items
) {
}
