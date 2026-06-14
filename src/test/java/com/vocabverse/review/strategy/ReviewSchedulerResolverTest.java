package com.vocabverse.review.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReviewSchedulerResolverTest {

    @Test
    void resolvesRegisteredSchedulers() {
        FixedIntervalScheduler fixedIntervalScheduler = new FixedIntervalScheduler();
        Sm2Scheduler sm2Scheduler = new Sm2Scheduler();
        ReviewSchedulerResolver resolver = new ReviewSchedulerResolver(List.of(fixedIntervalScheduler, sm2Scheduler));

        assertThat(resolver.resolve(ReviewSchedulerType.FIXED_INTERVAL)).isSameAs(fixedIntervalScheduler);
        assertThat(resolver.resolve(ReviewSchedulerType.SM2)).isSameAs(sm2Scheduler);
    }

    @Test
    void rejectsUnsupportedScheduler() {
        ReviewSchedulerResolver resolver = new ReviewSchedulerResolver(List.of(new FixedIntervalScheduler()));

        assertThatThrownBy(() -> resolver.resolve(ReviewSchedulerType.FSRS))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SCHEDULER_NOT_SUPPORTED);
    }
}
