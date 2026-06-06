package com.vocabverse.dictionary.dto.response;

import java.util.List;

public record DictionaryWordResponse(
        String word,
        String phonetic,
        String audioUrl,
        List<DictionaryMeaningResponse> meanings
) {
}
