package com.vocabverse.dictionary.dto.response;

import java.util.List;

public record DictionaryMeaningResponse(
        String partOfSpeech,
        List<DictionaryDefinitionResponse> definitions
) {
}
