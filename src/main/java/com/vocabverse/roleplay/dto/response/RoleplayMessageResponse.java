package com.vocabverse.roleplay.dto.response;

import com.vocabverse.roleplay.entity.RoleplayCorrection;
import com.vocabverse.roleplay.enums.RoleplayMessageSender;
import java.time.LocalDateTime;
import java.util.UUID;

public record RoleplayMessageResponse(
        UUID id,
        RoleplayMessageSender sender,
        String content,
        RoleplayCorrection correction,
        LocalDateTime createdAt
) {
}
