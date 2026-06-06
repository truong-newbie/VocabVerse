package com.vocabverse.admin.dto.response;

public record AdminSystemHealthResponse(
        String database,
        String redis,
        String rabbitmq
) {
}
