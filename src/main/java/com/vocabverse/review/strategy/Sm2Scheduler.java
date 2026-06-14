package com.vocabverse.review.strategy;

import com.vocabverse.collection.entity.CollectionReviewSettingEntity;
import com.vocabverse.learning.progress.entity.LearningProgressEntity;
import com.vocabverse.learning.progress.entity.LearningStatus;
import com.vocabverse.review.entity.ReviewResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class Sm2Scheduler implements ReviewScheduler {

    private static final BigDecimal DEFAULT_EASE_FACTOR = BigDecimal.valueOf(2.50);
    private static final BigDecimal MIN_EASE_FACTOR = BigDecimal.valueOf(1.30);
    private static final int MAX_INTERVAL_DAYS = 365;

    @Override
    public ReviewSchedulerType type() {
        return ReviewSchedulerType.SM2;
    }

    @Override
    public ReviewScheduleResult schedule(
            LearningProgressEntity progress,
            ReviewResult result,
            LocalDateTime reviewedAt,
            CollectionReviewSettingEntity setting
    ) {
        int quality = toQuality(result);
        BigDecimal easeFactor = calculateEaseFactor(resolveEaseFactor(progress), quality);
        int reviewCount = progress.getReviewCount() + 1;
        int lapseCount = progress.getLapseCount();
        int repetitionCount;
        int intervalDays;

        if (quality < 3) {
            repetitionCount = 0;
            intervalDays = 1;
            lapseCount++;
        } else {
            repetitionCount = progress.getRepetitionCount() + 1;
            intervalDays = calculateIntervalDays(repetitionCount, progress.getLastIntervalDays(), easeFactor);
        }

        return new ReviewScheduleResult(
                resolveStatus(result, repetitionCount),
                repetitionCount,
                easeFactor,
                intervalDays,
                lapseCount,
                reviewCount,
                reviewedAt.plusDays(intervalDays)
        );
    }

    private int toQuality(ReviewResult result) {
        return switch (result) {
            case AGAIN -> 2;
            case HARD -> 3;
            case GOOD -> 4;
            case EASY -> 5;
        };
    }

    private BigDecimal calculateEaseFactor(BigDecimal oldEaseFactor, int quality) {
        double difference = 5 - quality;
        double newEaseFactor = oldEaseFactor.doubleValue()
                + (0.1 - difference * (0.08 + difference * 0.02));
        BigDecimal roundedEaseFactor = BigDecimal.valueOf(newEaseFactor).setScale(2, RoundingMode.HALF_UP);
        return roundedEaseFactor.max(MIN_EASE_FACTOR);
    }

    private int calculateIntervalDays(int repetitionCount, int lastIntervalDays, BigDecimal easeFactor) {
        int intervalDays;
        if (repetitionCount <= 1) {
            intervalDays = 1;
        } else if (repetitionCount == 2) {
            intervalDays = 6;
        } else {
            int previousInterval = Math.max(1, lastIntervalDays);
            intervalDays = Math.round((float) (previousInterval * easeFactor.doubleValue()));
        }
        return Math.max(1, Math.min(intervalDays, MAX_INTERVAL_DAYS));
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

    private BigDecimal resolveEaseFactor(LearningProgressEntity progress) {
        return progress.getEaseFactor() == null ? DEFAULT_EASE_FACTOR : progress.getEaseFactor();
    }
}
