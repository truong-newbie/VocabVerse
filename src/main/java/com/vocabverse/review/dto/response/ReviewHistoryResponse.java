package com.vocabverse.review.dto.response;

import com.vocabverse.learning.progress.entity.LearningStatus;
import com.vocabverse.review.entity.ReviewResult;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewHistoryResponse(
        UUID vocabularyId,
        String term,
        ReviewResult result,
        LocalDateTime reviewedAt,
        LocalDateTime nextReviewAt,
        LearningStatus previousStatus,
        LearningStatus newStatus
) {
}
