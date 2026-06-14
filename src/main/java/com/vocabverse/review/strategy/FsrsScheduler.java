package com.vocabverse.review.strategy;

import com.vocabverse.collection.entity.CollectionReviewSettingEntity;
import com.vocabverse.learning.progress.entity.LearningProgressEntity;
import com.vocabverse.learning.progress.entity.LearningStatus;
import com.vocabverse.review.entity.ReviewResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

@Component
public class FsrsScheduler implements ReviewScheduler {

    private static final BigDecimal DEFAULT_EASE_FACTOR = BigDecimal.valueOf(2.50);
    private static final BigDecimal DEFAULT_DESIRED_RETENTION = BigDecimal.valueOf(0.900);
    private static final int DEFAULT_MAX_INTERVAL_DAYS = 3650;
    private static final double MIN_DIFFICULTY = 1.0;
    private static final double MAX_DIFFICULTY = 10.0;
    private static final double MIN_STABILITY = 0.1;
    private static final double MAX_STABILITY = 3650.0;

    @Override
    public ReviewSchedulerType type() {
        return ReviewSchedulerType.FSRS;
    }

    @Override
    public ReviewScheduleResult schedule(
            LearningProgressEntity progress,
            ReviewResult result,
            LocalDateTime reviewedAt,
            CollectionReviewSettingEntity setting
    ) {
        BigDecimal desiredRetention = resolveDesiredRetention(setting);
        int maxIntervalDays = resolveMaxIntervalDays(setting);
        FsrsMemoryState current = initializeOrLoad(progress, result);
        BigDecimal retrievability = calculateRetrievability(progress, reviewedAt, current.stability());
        FsrsMemoryState next = updateMemoryState(current, result, retrievability);
        int intervalDays = calculateIntervalDays(next.stability(), desiredRetention, maxIntervalDays);
        int repetitionCount = calculateRepetitionCount(progress, result);
        int lapseCount = progress.getLapseCount() + (result == ReviewResult.AGAIN ? 1 : 0);
        int reviewCount = progress.getReviewCount() + 1;

        return new ReviewScheduleResult(
                resolveStatus(result, repetitionCount),
                repetitionCount,
                resolveEaseFactor(progress),
                intervalDays,
                lapseCount,
                reviewCount,
                next.difficulty(),
                next.stability(),
                retrievability,
                reviewedAt.plusDays(intervalDays)
        );
    }

    private FsrsMemoryState initializeOrLoad(LearningProgressEntity progress, ReviewResult result) {
        if (progress.getFsrsDifficulty() != null && progress.getFsrsStability() != null) {
            return new FsrsMemoryState(progress.getFsrsDifficulty(), progress.getFsrsStability());
        }
        return switch (result) {
            case AGAIN -> state("7.000", "0.500");
            case HARD -> state("6.000", "1.000");
            case GOOD -> state("5.000", "2.500");
            case EASY -> state("4.000", "4.000");
        };
    }

    private BigDecimal calculateRetrievability(
            LearningProgressEntity progress,
            LocalDateTime reviewedAt,
            BigDecimal stability
    ) {
        if (progress.getLastReviewedAt() == null || stability == null || stability.signum() <= 0) {
            return BigDecimal.ONE.setScale(4, RoundingMode.HALF_UP);
        }
        long elapsedDays = Math.max(0, ChronoUnit.DAYS.between(progress.getLastReviewedAt(), reviewedAt));
        double value = Math.pow(1.0 + elapsedDays / (9.0 * stability.doubleValue()), -1.0);
        return BigDecimal.valueOf(clamp(value, 0.0, 1.0)).setScale(4, RoundingMode.HALF_UP);
    }

    private FsrsMemoryState updateMemoryState(
            FsrsMemoryState current,
            ReviewResult result,
            BigDecimal retrievability
    ) {
        double oldDifficulty = current.difficulty().doubleValue();
        double newDifficulty = clamp(oldDifficulty + difficultyModifier(result), MIN_DIFFICULTY, MAX_DIFFICULTY);
        double oldStability = current.stability().doubleValue();
        double retrievabilityValue = retrievability.doubleValue();
        double newStability = switch (result) {
            case AGAIN -> Math.max(0.5, oldStability * 0.5);
            case HARD -> oldStability * (1.2 - newDifficulty * 0.02);
            case GOOD -> oldStability * (1.0 + (1.0 - retrievabilityValue) * 1.5 + (10.0 - newDifficulty) * 0.05);
            case EASY -> oldStability * (1.0 + (1.0 - retrievabilityValue) * 2.0 + (10.0 - newDifficulty) * 0.08 + 0.3);
        };
        return new FsrsMemoryState(
                scaled(clamp(newDifficulty, MIN_DIFFICULTY, MAX_DIFFICULTY), 3),
                scaled(clamp(newStability, MIN_STABILITY, MAX_STABILITY), 3)
        );
    }

    private double difficultyModifier(ReviewResult result) {
        return switch (result) {
            case AGAIN -> 1.2;
            case HARD -> 0.6;
            case GOOD -> -0.2;
            case EASY -> -0.8;
        };
    }

    private int calculateIntervalDays(
            BigDecimal stability,
            BigDecimal desiredRetention,
            int maxIntervalDays
    ) {
        double interval = 9.0 * stability.doubleValue() * (1.0 / desiredRetention.doubleValue() - 1.0);
        int days = Math.max(1, (int) Math.round(interval));
        return Math.min(days, maxIntervalDays);
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

    private BigDecimal resolveDesiredRetention(CollectionReviewSettingEntity setting) {
        if (setting == null || setting.getFsrsDesiredRetention() == null) {
            return DEFAULT_DESIRED_RETENTION;
        }
        return setting.getFsrsDesiredRetention();
    }

    private int resolveMaxIntervalDays(CollectionReviewSettingEntity setting) {
        if (setting == null || setting.getFsrsMaxIntervalDays() <= 0) {
            return DEFAULT_MAX_INTERVAL_DAYS;
        }
        return setting.getFsrsMaxIntervalDays();
    }

    private BigDecimal resolveEaseFactor(LearningProgressEntity progress) {
        return progress.getEaseFactor() == null ? DEFAULT_EASE_FACTOR : progress.getEaseFactor();
    }

    private FsrsMemoryState state(String difficulty, String stability) {
        return new FsrsMemoryState(new BigDecimal(difficulty), new BigDecimal(stability));
    }

    private BigDecimal scaled(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    private record FsrsMemoryState(BigDecimal difficulty, BigDecimal stability) {
    }
}
