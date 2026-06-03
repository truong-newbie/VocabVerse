package com.vocabverse.common.constant;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INTERNAL_SERVER_ERROR(
            "INTERNAL_SERVER_ERROR",
            "Unexpected server error",
            HttpStatus.INTERNAL_SERVER_ERROR
    ),
    INVALID_INPUT(
            "INVALID_INPUT",
            "Invalid input",
            HttpStatus.BAD_REQUEST
    ),
    VALIDATION_ERROR(
            "VALIDATION_ERROR",
            "Validation failed",
            HttpStatus.BAD_REQUEST
    ),
    ACCESS_DENIED(
            "ACCESS_DENIED",
            "Access denied",
            HttpStatus.FORBIDDEN
    ),
    AUTH_INVALID_CREDENTIAL(
            "AUTH_INVALID_CREDENTIAL",
            "Invalid email or password",
            HttpStatus.UNAUTHORIZED
    ),
    AUTH_TOKEN_EXPIRED(
            "AUTH_TOKEN_EXPIRED",
            "Authentication token has expired",
            HttpStatus.UNAUTHORIZED
    ),
    AUTH_EMAIL_ALREADY_EXISTS(
            "AUTH_EMAIL_ALREADY_EXISTS",
            "Email already exists",
            HttpStatus.CONFLICT
    ),
    UNAUTHORIZED(
            "UNAUTHORIZED",
            "Unauthorized",
            HttpStatus.UNAUTHORIZED
    ),
    REFRESH_TOKEN_INVALID(
            "REFRESH_TOKEN_INVALID",
            "Refresh token is invalid",
            HttpStatus.UNAUTHORIZED
    ),
    REFRESH_TOKEN_EXPIRED(
            "REFRESH_TOKEN_EXPIRED",
            "Refresh token has expired",
            HttpStatus.UNAUTHORIZED
    ),
    REFRESH_TOKEN_REVOKED(
            "REFRESH_TOKEN_REVOKED",
            "Refresh token has been revoked",
            HttpStatus.UNAUTHORIZED
    ),
    USER_NOT_FOUND(
            "USER_NOT_FOUND",
            "User not found",
            HttpStatus.NOT_FOUND
    ),
    COLLECTION_NOT_FOUND(
            "COLLECTION_NOT_FOUND",
            "Collection not found",
            HttpStatus.NOT_FOUND
    ),
    COLLECTION_SYSTEM_FORBIDDEN(
            "COLLECTION_SYSTEM_FORBIDDEN",
            "System collection cannot be managed from user API",
            HttpStatus.FORBIDDEN
    ),
    VOCABULARY_NOT_FOUND(
            "VOCABULARY_NOT_FOUND",
            "Vocabulary not found",
            HttpStatus.NOT_FOUND
    ),
    DUPLICATE_WORD(
            "DUPLICATE_WORD",
            "Word already exists in the collection",
            HttpStatus.CONFLICT
    ),
    INVALID_INPUT_FORMAT(
            "INVALID_INPUT_FORMAT",
            "Invalid input format",
            HttpStatus.BAD_REQUEST
    ),
    AI_PROCESSING_FAILED(
            "AI_PROCESSING_FAILED",
            "AI processing failed",
            HttpStatus.INTERNAL_SERVER_ERROR
    ),
    VIDEO_PROCESSING_FAILED(
            "VIDEO_PROCESSING_FAILED",
            "Video processing failed",
            HttpStatus.INTERNAL_SERVER_ERROR
    ),
    PDF_GENERATION_FAILED(
            "PDF_GENERATION_FAILED",
            "PDF generation failed",
            HttpStatus.INTERNAL_SERVER_ERROR
    );

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
