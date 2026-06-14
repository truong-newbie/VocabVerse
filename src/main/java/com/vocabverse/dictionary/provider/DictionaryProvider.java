package com.vocabverse.dictionary.provider;

import com.vocabverse.dictionary.dto.response.DictionaryWordResponse;

public interface DictionaryProvider {

    DictionaryWordResponse search(String word);
}
