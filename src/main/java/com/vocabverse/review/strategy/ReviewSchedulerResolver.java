package com.vocabverse.review.strategy;

import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ReviewSchedulerResolver {

    private final Map<ReviewSchedulerType, ReviewScheduler> schedulers;

    public ReviewSchedulerResolver(List<ReviewScheduler> schedulerList) {
        this.schedulers = schedulerList.stream()
                .collect(Collectors.toMap(ReviewScheduler::type, Function.identity()));
    }

    public ReviewScheduler resolve(ReviewSchedulerType type) {
        ReviewScheduler scheduler = schedulers.get(type);
        if (scheduler == null) {
            throw new BusinessException(ErrorCode.SCHEDULER_NOT_SUPPORTED);
        }
        return scheduler;
    }
}
