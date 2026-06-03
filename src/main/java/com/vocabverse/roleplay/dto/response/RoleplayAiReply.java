package com.vocabverse.roleplay.dto.response;

import com.vocabverse.roleplay.entity.RoleplayCorrection;

public record RoleplayAiReply(
        String reply,
        RoleplayCorrection correction
) {
}
