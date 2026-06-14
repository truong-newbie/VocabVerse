package com.vocabverse.ai.service;

import com.vocabverse.ai.dto.response.AiNormalizeQuotaResponse;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.user.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AiNormalizeQuotaService {

    private static final String KEY_PREFIX = "ai:normalize:trial:";

    private final StringRedisTemplate stringRedisTemplate;
    private final UserRepository userRepository;

    @Value("${ai.normalize.trial.daily-limit:3}")
    private int dailyLimit;

    public boolean shouldApplySystemTrialQuota(String userApiKey) {
        return !StringUtils.hasText(userApiKey);
    }

    public void consumeTrialQuota() {
        UserEntity user = getCurrentUser();
        String key = cacheKey(user.getId());

        Long used;
        try {
            used = stringRedisTemplate.opsForValue().increment(key);
            if (used == 1L) {
                stringRedisTemplate.expire(key, Duration.between(LocalDateTime.now(), resetAt()));
            }
        } catch (Exception exception) {
            throw new BusinessException(
                    ErrorCode.AI_PROVIDER_UNAVAILABLE,
                    "AI normalize quota is unavailable",
                    exception
            );
        }
        if (used == null) {
            throw new BusinessException(ErrorCode.AI_PROVIDER_UNAVAILABLE, "AI normalize quota is unavailable");
        }
        if (used > dailyLimit) {
            throw new BusinessException(ErrorCode.AI_DAILY_LIMIT_REACHED);
        }
    }

    public AiNormalizeQuotaResponse getCurrentQuota() {
        UserEntity user = getCurrentUser();
        int used = readUsed(cacheKey(user.getId()));
        return new AiNormalizeQuotaResponse(
                dailyLimit,
                used,
                Math.max(0, dailyLimit - used),
                resetAt()
        );
    }

    private int readUsed(String key) {
        String value;
        try {
            value = stringRedisTemplate.opsForValue().get(key);
        } catch (Exception exception) {
            throw new BusinessException(
                    ErrorCode.AI_PROVIDER_UNAVAILABLE,
                    "AI normalize quota is unavailable",
                    exception
            );
        }
        if (!StringUtils.hasText(value)) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(value));
        } catch (NumberFormatException exception) {
            return dailyLimit;
        }
    }

    private UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private String cacheKey(UUID userId) {
        return KEY_PREFIX + userId + ":" + LocalDate.now();
    }

    private LocalDateTime resetAt() {
        return LocalDate.now().plusDays(1).atStartOfDay();
    }
}
