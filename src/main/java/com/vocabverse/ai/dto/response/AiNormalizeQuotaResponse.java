package com.vocabverse.ai.dto.response;

import java.time.LocalDateTime;

public record AiNormalizeQuotaResponse(
        int dailyLimit,
        int used,
        int remaining,
        LocalDateTime resetAt
) {
}
