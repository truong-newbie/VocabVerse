package com.vocabverse.roleplay.dto.response;

import com.vocabverse.roleplay.enums.RoleplayDifficulty;
import com.vocabverse.roleplay.enums.RoleplaySessionStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RoleplaySessionResponse(
        UUID id,
        String topic,
        RoleplayDifficulty difficulty,
        String persona,
        String scenario,
        RoleplaySessionStatus status,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<RoleplayMessageResponse> messages,
        RoleplayReportResponse report
) {
}
