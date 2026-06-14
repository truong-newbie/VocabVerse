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
    PUBLIC_COLLECTION_NOT_FOUND(
            "PUBLIC_COLLECTION_NOT_FOUND",
            "Public collection not found",
            HttpStatus.NOT_FOUND
    ),
    PUBLIC_COLLECTION_ACCESS_DENIED(
            "PUBLIC_COLLECTION_ACCESS_DENIED",
            "Access to public collection is denied",
            HttpStatus.FORBIDDEN
    ),
    COLLECTION_NOT_PUBLIC(
            "COLLECTION_NOT_PUBLIC",
            "Collection is not public",
            HttpStatus.NOT_FOUND
    ),
    PUBLIC_COLLECTION_CLONE_FAILED(
            "PUBLIC_COLLECTION_CLONE_FAILED",
            "Failed to clone public collection",
            HttpStatus.INTERNAL_SERVER_ERROR
    ),
    COLLECTION_REVIEW_SETTING_NOT_FOUND(
            "COLLECTION_REVIEW_SETTING_NOT_FOUND",
            "Collection review setting not found",
            HttpStatus.NOT_FOUND
    ),
    INVALID_REVIEW_INTERVALS(
            "INVALID_REVIEW_INTERVALS",
            "Review intervals are invalid",
            HttpStatus.BAD_REQUEST
    ),
    REVIEW_SETTINGS_DISABLED(
            "REVIEW_SETTINGS_DISABLED",
            "Review settings are disabled",
            HttpStatus.CONFLICT
    ),
    COLLECTION_REVIEW_RESET_FAILED(
            "COLLECTION_REVIEW_RESET_FAILED",
            "Failed to reset collection review schedule",
            HttpStatus.INTERNAL_SERVER_ERROR
    ),
    VOCABULARY_NOT_FOUND(
            "VOCABULARY_NOT_FOUND",
            "Vocabulary not found",
            HttpStatus.NOT_FOUND
    ),
    DICTIONARY_WORD_NOT_FOUND(
            "DICTIONARY_WORD_NOT_FOUND",
            "Dictionary word not found",
            HttpStatus.NOT_FOUND
    ),
    DICTIONARY_PROVIDER_UNAVAILABLE(
            "DICTIONARY_PROVIDER_UNAVAILABLE",
            "Dictionary provider is temporarily unavailable",
            HttpStatus.SERVICE_UNAVAILABLE
    ),
    DICTIONARY_TIMEOUT(
            "DICTIONARY_TIMEOUT",
            "Dictionary request timed out",
            HttpStatus.GATEWAY_TIMEOUT
    ),
    DICTIONARY_INVALID_RESPONSE(
            "DICTIONARY_INVALID_RESPONSE",
            "Dictionary provider response is invalid",
            HttpStatus.BAD_GATEWAY
    ),
    LEARNING_PROGRESS_NOT_FOUND(
            "LEARNING_PROGRESS_NOT_FOUND",
            "Learning progress not found",
            HttpStatus.NOT_FOUND
    ),
    FLASHCARD_SESSION_NOT_FOUND(
            "FLASHCARD_SESSION_NOT_FOUND",
            "Flashcard session not found",
            HttpStatus.NOT_FOUND
    ),
    FLASHCARD_CARD_NOT_FOUND(
            "FLASHCARD_CARD_NOT_FOUND",
            "Flashcard card not found in session",
            HttpStatus.NOT_FOUND
    ),
    FLASHCARD_SESSION_COMPLETED(
            "FLASHCARD_SESSION_COMPLETED",
            "Flashcard session is already completed",
            HttpStatus.CONFLICT
    ),
    FLASHCARD_CARD_ALREADY_ANSWERED(
            "FLASHCARD_CARD_ALREADY_ANSWERED",
            "Flashcard card has already been answered",
            HttpStatus.CONFLICT
    ),
    QUIZ_SESSION_NOT_FOUND(
            "QUIZ_SESSION_NOT_FOUND",
            "Quiz session not found",
            HttpStatus.NOT_FOUND
    ),
    QUIZ_QUESTION_NOT_FOUND(
            "QUIZ_QUESTION_NOT_FOUND",
            "Quiz question not found in session",
            HttpStatus.NOT_FOUND
    ),
    QUIZ_SESSION_COMPLETED(
            "QUIZ_SESSION_COMPLETED",
            "Quiz session is already completed",
            HttpStatus.CONFLICT
    ),
    QUIZ_QUESTION_ALREADY_ANSWERED(
            "QUIZ_QUESTION_ALREADY_ANSWERED",
            "Quiz question has already been answered",
            HttpStatus.CONFLICT
    ),
    TYPING_SESSION_NOT_FOUND(
            "TYPING_SESSION_NOT_FOUND",
            "Typing session not found",
            HttpStatus.NOT_FOUND
    ),
    TYPING_QUESTION_NOT_FOUND(
            "TYPING_QUESTION_NOT_FOUND",
            "Typing question not found in session",
            HttpStatus.NOT_FOUND
    ),
    TYPING_SESSION_COMPLETED(
            "TYPING_SESSION_COMPLETED",
            "Typing session is already completed",
            HttpStatus.CONFLICT
    ),
    TYPING_QUESTION_ALREADY_ANSWERED(
            "TYPING_QUESTION_ALREADY_ANSWERED",
            "Typing question has already been answered",
            HttpStatus.CONFLICT
    ),
    NOTIFICATION_NOT_FOUND(
            "NOTIFICATION_NOT_FOUND",
            "Notification not found",
            HttpStatus.NOT_FOUND
    ),
    NOTIFICATION_ALREADY_EXISTS(
            "NOTIFICATION_ALREADY_EXISTS",
            "Notification already exists for today",
            HttpStatus.CONFLICT
    ),
    ROLEPLAY_SESSION_NOT_FOUND(
            "ROLEPLAY_SESSION_NOT_FOUND",
            "Roleplay session not found",
            HttpStatus.NOT_FOUND
    ),
    ROLEPLAY_SESSION_COMPLETED(
            "ROLEPLAY_SESSION_COMPLETED",
            "Roleplay session is already completed",
            HttpStatus.CONFLICT
    ),
    ADMIN_LAST_ADMIN_REMOVAL_NOT_ALLOWED(
            "ADMIN_LAST_ADMIN_REMOVAL_NOT_ALLOWED",
            "Cannot remove the last admin",
            HttpStatus.CONFLICT
    ),
    SHADOWING_LESSON_NOT_FOUND(
            "SHADOWING_LESSON_NOT_FOUND",
            "Shadowing lesson not found",
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
    AI_PROVIDER_UNAVAILABLE(
            "AI_PROVIDER_UNAVAILABLE",
            "AI service is temporarily unavailable",
            HttpStatus.SERVICE_UNAVAILABLE
    ),
    AI_PROVIDER_NOT_AVAILABLE(
            "AI_PROVIDER_NOT_AVAILABLE",
            "AI provider is not available",
            HttpStatus.SERVICE_UNAVAILABLE
    ),
    AI_NORMALIZE_FAILED(
            "AI_NORMALIZE_FAILED",
            "AI normalization failed",
            HttpStatus.INTERNAL_SERVER_ERROR
    ),
    AI_RESPONSE_INVALID(
            "AI_RESPONSE_INVALID",
            "AI response is invalid",
            HttpStatus.BAD_GATEWAY
    ),
    AI_TIMEOUT(
            "AI_TIMEOUT",
            "AI request timed out",
            HttpStatus.GATEWAY_TIMEOUT
    ),
    AI_RATE_LIMITED(
            "AI_RATE_LIMITED",
            "AI provider rate limit exceeded",
            HttpStatus.TOO_MANY_REQUESTS
    ),
    AI_DAILY_LIMIT_REACHED(
            "AI_DAILY_LIMIT_REACHED",
            "Daily AI normalize trial limit reached",
            HttpStatus.TOO_MANY_REQUESTS
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
