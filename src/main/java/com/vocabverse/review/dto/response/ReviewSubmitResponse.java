package com.vocabverse.review.dto.response;

import com.vocabverse.learning.progress.entity.LearningStatus;
import java.time.LocalDateTime;

public record ReviewSubmitResponse(
        LearningStatus status,
        LocalDateTime nextReviewAt,
        int repetitionCount
) {
}
