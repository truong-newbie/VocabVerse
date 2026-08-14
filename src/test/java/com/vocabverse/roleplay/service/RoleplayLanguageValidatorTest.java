package com.vocabverse.roleplay.service;

import com.vocabverse.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoleplayLanguageValidatorTest {

    private final RoleplayLanguageValidator validator = new RoleplayLanguageValidator();

    @Test
    void validateEnglishMessageAllowsEnglishText() {
        assertDoesNotThrow(() -> validator.validateEnglishMessage("I would like to book a room for two nights."));
    }

    @Test
    void validateEnglishMessageRejectsVietnameseText() {
        assertThrows(BusinessException.class,
                () -> validator.validateEnglishMessage("Tôi muốn đặt phòng khách sạn."));
    }

    @Test
    void validateEnglishMessageRejectsMessageWithoutEnglishWords() {
        assertThrows(BusinessException.class,
                () -> validator.validateEnglishMessage("12345 !!!"));
    }
}
