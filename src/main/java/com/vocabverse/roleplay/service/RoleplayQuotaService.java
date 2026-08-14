package com.vocabverse.roleplay.service;

import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.roleplay.enums.RoleplayMessageSender;
import com.vocabverse.roleplay.repository.RoleplayMessageRepository;
import com.vocabverse.roleplay.repository.RoleplaySessionRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleplayQuotaService {

    private final RoleplaySessionRepository roleplaySessionRepository;
    private final RoleplayMessageRepository roleplayMessageRepository;

    @Value("${roleplay.quota.daily-session-limit:10}")
    private int dailySessionLimit;

    @Value("${roleplay.quota.daily-message-limit:80}")
    private int dailyMessageLimit;

    public void assertCanCreateSession(UUID userId) {
        if (dailySessionLimit <= 0) {
            return;
        }
        long used = roleplaySessionRepository.countByUserIdAndCreatedAtGreaterThanEqual(userId, todayStart());
        if (used >= dailySessionLimit) {
            throw new BusinessException(ErrorCode.ROLEPLAY_DAILY_LIMIT_REACHED,
                    "Daily roleplay session limit reached");
        }
    }

    public void assertCanSendMessage(UUID userId) {
        if (dailyMessageLimit <= 0) {
            return;
        }
        long used = roleplayMessageRepository.countByUserIdAndSenderSince(
                userId,
                RoleplayMessageSender.USER,
                todayStart()
        );
        if (used >= dailyMessageLimit) {
            throw new BusinessException(ErrorCode.ROLEPLAY_DAILY_LIMIT_REACHED,
                    "Daily roleplay message limit reached");
        }
    }

    private LocalDateTime todayStart() {
        return LocalDate.now().atStartOfDay();
    }
}
