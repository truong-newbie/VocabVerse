package com.vocabverse.dictionary.dto.response;

import java.util.List;

public record DictionaryDefinitionResponse(
        String definition,
        String example,
        List<String> synonyms,
        List<String> antonyms
) {
}
