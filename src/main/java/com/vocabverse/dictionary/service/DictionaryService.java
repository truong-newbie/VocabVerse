package com.vocabverse.dictionary.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.dictionary.dto.response.DictionaryWordResponse;
import com.vocabverse.dictionary.provider.DictionaryProvider;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class DictionaryService {

    private static final int MAX_WORD_LENGTH = 100;
    private static final Duration CACHE_TTL = Duration.ofHours(24);
    private static final Pattern ENGLISH_WORD_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z\\s'-]*$");

    private final DictionaryProvider dictionaryProvider;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public DictionaryWordResponse search(String word) {
        String normalizedWord = normalizeWord(word);
        String cacheKey = cacheKey(normalizedWord);

        Optional<DictionaryWordResponse> cached = readCache(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }

        DictionaryWordResponse response = dictionaryProvider.search(normalizedWord);
        writeCache(cacheKey, response);
        return response;
    }

    private String normalizeWord(String word) {
        if (!StringUtils.hasText(word)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "word is required");
        }

        String normalized = word.trim().replaceAll("\\s+", " ");
        if (normalized.length() > MAX_WORD_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "word must be at most 100 characters");
        }
        if (!ENGLISH_WORD_PATTERN.matcher(normalized).matches()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "word must contain English letters only");
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    private Optional<DictionaryWordResponse> readCache(String cacheKey) {
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (!StringUtils.hasText(cached)) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(cached, DictionaryWordResponse.class));
        } catch (Exception exception) {
            log.warn("Dictionary cache read failed key={}", cacheKey);
            return Optional.empty();
        }
    }

    private void writeCache(String cacheKey, DictionaryWordResponse response) {
        try {
            stringRedisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(response), CACHE_TTL);
        } catch (Exception exception) {
            log.warn("Dictionary cache write failed key={}", cacheKey);
        }
    }

    private String cacheKey(String normalizedWord) {
        return "dictionary:word:" + normalizedWord;
    }
}
