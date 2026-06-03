package com.vocabverse.review.strategy;

import com.vocabverse.learning.progress.entity.LearningProgressEntity;
import com.vocabverse.review.entity.ReviewResult;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class SimpleReviewIntervalStrategy implements ReviewIntervalStrategy {

    @Override
    public LocalDateTime calculateNextReviewDate(
            LearningProgressEntity progress,
            ReviewResult result,
            LocalDateTime reviewedAt
    ) {
        return reviewedAt.plusDays(resolveIntervalDays(progress, result));
    }

    private int resolveIntervalDays(LearningProgressEntity progress, ReviewResult result) {
        int repetitionCount = progress.getRepetitionCount();
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
}
