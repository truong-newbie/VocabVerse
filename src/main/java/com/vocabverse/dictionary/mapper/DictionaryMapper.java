package com.vocabverse.dictionary.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.dictionary.dto.response.DictionaryDefinitionResponse;
import com.vocabverse.dictionary.dto.response.DictionaryMeaningResponse;
import com.vocabverse.dictionary.dto.response.DictionaryWordResponse;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DictionaryMapper {

    public DictionaryWordResponse toResponse(JsonNode providerRoot) {
        JsonNode entry = resolveEntry(providerRoot);
        String word = text(entry.path("word"));
        String phonetic = resolvePhonetic(entry);
        String audioUrl = resolveAudioUrl(entry);
        List<DictionaryMeaningResponse> meanings = toMeanings(entry.path("meanings"));

        if (!StringUtils.hasText(word) || meanings.isEmpty()) {
            throw new BusinessException(ErrorCode.DICTIONARY_INVALID_RESPONSE);
        }

        return new DictionaryWordResponse(word, phonetic, audioUrl, meanings);
    }

    private JsonNode resolveEntry(JsonNode providerRoot) {
        if (providerRoot == null || providerRoot.isNull()) {
            throw new BusinessException(ErrorCode.DICTIONARY_INVALID_RESPONSE);
        }
        if (providerRoot.isArray() && providerRoot.size() > 0) {
            return providerRoot.get(0);
        }
        if (providerRoot.isObject() && providerRoot.has("word")) {
            return providerRoot;
        }
        throw new BusinessException(ErrorCode.DICTIONARY_INVALID_RESPONSE);
    }

    private String resolvePhonetic(JsonNode entry) {
        String phonetic = text(entry.path("phonetic"));
        if (StringUtils.hasText(phonetic)) {
            return phonetic;
        }

        JsonNode phonetics = entry.path("phonetics");
        if (!phonetics.isArray()) {
            return null;
        }
        for (JsonNode item : phonetics) {
            String text = text(item.path("text"));
            if (StringUtils.hasText(text)) {
                return text;
            }
        }
        return null;
    }

    private String resolveAudioUrl(JsonNode entry) {
        JsonNode phonetics = entry.path("phonetics");
        if (!phonetics.isArray()) {
            return null;
        }
        for (JsonNode item : phonetics) {
            String audio = text(item.path("audio"));
            if (StringUtils.hasText(audio)) {
                return audio;
            }
        }
        return null;
    }

    private List<DictionaryMeaningResponse> toMeanings(JsonNode meaningsNode) {
        if (!meaningsNode.isArray()) {
            return List.of();
        }

        List<DictionaryMeaningResponse> meanings = new ArrayList<>();
        for (JsonNode meaningNode : meaningsNode) {
            String partOfSpeech = text(meaningNode.path("partOfSpeech"));
            List<DictionaryDefinitionResponse> definitions = toDefinitions(meaningNode);
            if (StringUtils.hasText(partOfSpeech) && !definitions.isEmpty()) {
                meanings.add(new DictionaryMeaningResponse(partOfSpeech, definitions));
            }
        }
        return List.copyOf(meanings);
    }

    private List<DictionaryDefinitionResponse> toDefinitions(JsonNode meaningNode) {
        JsonNode definitionsNode = meaningNode.path("definitions");
        if (!definitionsNode.isArray()) {
            return List.of();
        }

        List<String> meaningSynonyms = toTextList(meaningNode.path("synonyms"));
        List<String> meaningAntonyms = toTextList(meaningNode.path("antonyms"));
        List<DictionaryDefinitionResponse> definitions = new ArrayList<>();

        for (JsonNode definitionNode : definitionsNode) {
            String definition = text(definitionNode.path("definition"));
            if (!StringUtils.hasText(definition)) {
                continue;
            }
            List<String> synonyms = merge(meaningSynonyms, toTextList(definitionNode.path("synonyms")));
            List<String> antonyms = merge(meaningAntonyms, toTextList(definitionNode.path("antonyms")));
            definitions.add(new DictionaryDefinitionResponse(
                    definition,
                    text(definitionNode.path("example")),
                    synonyms,
                    antonyms
            ));
        }
        return List.copyOf(definitions);
    }

    private List<String> merge(List<String> first, List<String> second) {
        Set<String> values = new LinkedHashSet<>();
        values.addAll(first);
        values.addAll(second);
        return List.copyOf(values);
    }

    private List<String> toTextList(JsonNode node) {
        if (!node.isArray()) {
            return List.of();
        }

        List<String> values = new ArrayList<>();
        for (JsonNode item : node) {
            String value = text(item);
            if (StringUtils.hasText(value)) {
                values.add(value);
            }
        }
        return List.copyOf(values);
    }

    private String text(JsonNode node) {
        return node != null && node.isTextual() ? node.asText().trim() : null;
    }
}
