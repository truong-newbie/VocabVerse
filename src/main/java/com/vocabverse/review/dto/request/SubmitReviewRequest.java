package com.vocabverse.review.dto.request;

import com.vocabverse.review.entity.ReviewResult;
import jakarta.validation.constraints.NotNull;

public record SubmitReviewRequest(
        @NotNull
        ReviewResult result
) {
}
