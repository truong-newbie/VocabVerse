package com.vocabverse.review.strategy;

import com.vocabverse.collection.entity.CollectionReviewSettingEntity;
import com.vocabverse.learning.progress.entity.LearningProgressEntity;
import com.vocabverse.learning.progress.entity.LearningStatus;
import com.vocabverse.review.entity.ReviewResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FixedIntervalScheduler implements ReviewScheduler {

    @Override
    public ReviewSchedulerType type() {
        return ReviewSchedulerType.FIXED_INTERVAL;
    }

    @Override
    public ReviewScheduleResult schedule(
            LearningProgressEntity progress,
            ReviewResult result,
            LocalDateTime reviewedAt,
            CollectionReviewSettingEntity setting
    ) {
        int repetitionCount = calculateRepetitionCount(progress, result);
        LearningStatus status = resolveStatus(result, repetitionCount);
        int intervalDays = setting == null
                ? resolveDefaultIntervalDays(repetitionCount, result)
                : resolveCollectionIntervalDays(setting, repetitionCount, result);

        return new ReviewScheduleResult(
                status,
                repetitionCount,
                resolveEaseFactor(progress),
                intervalDays,
                progress.getLapseCount() + (result == ReviewResult.AGAIN ? 1 : 0),
                progress.getReviewCount() + 1,
                reviewedAt.plusDays(intervalDays)
        );
    }

    private int calculateRepetitionCount(LearningProgressEntity progress, ReviewResult result) {
        if (result == ReviewResult.AGAIN) {
            return 0;
        }
        return progress.getRepetitionCount() + 1;
    }

    private LearningStatus resolveStatus(ReviewResult result, int repetitionCount) {
        if (result == ReviewResult.EASY && repetitionCount >= 5) {
            return LearningStatus.MASTERED;
        }
        if (repetitionCount >= 2) {
            return LearningStatus.REVIEWING;
        }
        return LearningStatus.LEARNING;
    }

    private int resolveCollectionIntervalDays(
            CollectionReviewSettingEntity setting,
            int repetitionCount,
            ReviewResult result
    ) {
        List<Integer> intervals = setting.getIntervalsJson();
        if (intervals == null || intervals.isEmpty()) {
            return 1;
        }
        if (result == ReviewResult.AGAIN) {
            return intervals.get(0);
        }
        int index = Math.max(0, repetitionCount - 1);
        return intervals.get(Math.min(index, intervals.size() - 1));
    }

    private int resolveDefaultIntervalDays(int repetitionCount, ReviewResult result) {
        return switch (result) {
            case AGAIN -> 1;
            case HARD -> repetitionCount <= 1 ? 1 : 3;
            case GOOD -> switch (Math.min(repetitionCount, 4)) {
                case 0, 1 -> 1;
                case 2 -> 3;
                case 3 -> 7;
                default -> 14;
            };
            case EASY -> switch (Math.min(repetitionCount, 4)) {
                case 0, 1 -> 3;
                case 2 -> 7;
                case 3 -> 14;
                default -> 30;
            };
        };
    }

    private BigDecimal resolveEaseFactor(LearningProgressEntity progress) {
        return progress.getEaseFactor() == null ? BigDecimal.valueOf(2.50) : progress.getEaseFactor();
    }
}
