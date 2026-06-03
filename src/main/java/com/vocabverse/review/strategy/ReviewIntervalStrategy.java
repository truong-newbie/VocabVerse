package com.vocabverse.review.strategy;

import com.vocabverse.learning.progress.entity.LearningProgressEntity;
import com.vocabverse.review.entity.ReviewResult;
import java.time.LocalDateTime;

public interface ReviewIntervalStrategy {

    LocalDateTime calculateNextReviewDate(LearningProgressEntity progress, ReviewResult result, LocalDateTime reviewedAt);
}
