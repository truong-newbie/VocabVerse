package com.vocabverse.admin.dto.response;

public record AdminSystemHealthResponse(
        AdminHealthComponentResponse database,
        AdminHealthComponentResponse redis,
        AdminHealthComponentResponse rabbitmq
) {
}
