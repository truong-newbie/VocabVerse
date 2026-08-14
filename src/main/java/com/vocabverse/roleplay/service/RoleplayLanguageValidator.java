package com.vocabverse.roleplay.service;

import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class RoleplayLanguageValidator {

    private static final Pattern VIETNAMESE_CHARACTERS = Pattern.compile(
            ".*[ăâđêôơưĂÂĐÊÔƠƯáàảãạấầẩẫậắằẳẵặéèẻẽẹếềểễệíìỉĩịóòỏõọốồổỗộớờởỡợúùủũụứừửữựýỳỷỹỵ].*"
    );
    private static final Pattern LATIN_WORD = Pattern.compile(".*[A-Za-z]{2,}.*");

    public void validateEnglishMessage(String message) {
        String value = message == null ? "" : message.trim();
        if (value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Please type an English message");
        }
        if (VIETNAMESE_CHARACTERS.matcher(value).matches()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Please use English in roleplay practice");
        }
        if (!LATIN_WORD.matcher(value).matches()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Please use English words in roleplay practice");
        }
    }
}
