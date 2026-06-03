package com.vocabverse.roleplay.dto.response;

import java.util.List;

public record RoleplaySessionPageResponse(
        List<RoleplaySessionResponse> items,
        int page,
        int size,
        long totalItems,
        int totalPages
) {
}
