package com.vocabverse.ai.parser;

import com.vocabverse.ai.dto.response.NormalizeBulkVocabularyItemResponse;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class BulkVocabularyJsonParser {

    private final VocabularyNormalizeJsonParser vocabularyNormalizeJsonParser;

    public BulkVocabularyJsonParser(VocabularyNormalizeJsonParser vocabularyNormalizeJsonParser) {
        this.vocabularyNormalizeJsonParser = vocabularyNormalizeJsonParser;
    }

    public List<NormalizeBulkVocabularyItemResponse> parse(String content) {
        return vocabularyNormalizeJsonParser.parseBulk(content);
    }
}
