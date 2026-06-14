package com.vocabverse.review.strategy;

import com.vocabverse.collection.entity.CollectionReviewSettingEntity;
import com.vocabverse.learning.progress.entity.LearningProgressEntity;
import com.vocabverse.review.entity.ReviewResult;
import java.time.LocalDateTime;

public interface ReviewScheduler {

    ReviewSchedulerType type();

    ReviewScheduleResult schedule(
            LearningProgressEntity progress,
            ReviewResult result,
            LocalDateTime reviewedAt,
            CollectionReviewSettingEntity setting
    );
}
