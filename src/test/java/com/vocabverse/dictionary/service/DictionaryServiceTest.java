package com.vocabverse.dictionary.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.dictionary.dto.response.DictionaryMeaningResponse;
import com.vocabverse.dictionary.dto.response.DictionaryWordResponse;
import com.vocabverse.dictionary.provider.DictionaryProvider;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class DictionaryServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void searchNormalizesWordAndCachesProviderResponse() {
        DictionaryWordResponse providerResponse = new DictionaryWordResponse(
                "abandon",
                "/uh-BAN-duhn/",
                "https://example.com/audio.mp3",
                List.of(new DictionaryMeaningResponse("verb", List.of()))
        );
        DictionaryProvider provider = word -> {
            assertThat(word).isEqualTo("abandon");
            return providerResponse;
        };
        StringRedisTemplate redisTemplate = mockRedisTemplate(null);
        DictionaryService service = new DictionaryService(provider, redisTemplate, objectMapper);

        DictionaryWordResponse response = service.search("  Abandon  ");

        assertThat(response).isEqualTo(providerResponse);
        verify(redisTemplate.opsForValue()).get("dictionary:word:abandon");
        verify(redisTemplate.opsForValue()).set(
                eq("dictionary:word:abandon"),
                anyString(),
                eq(Duration.ofHours(24))
        );
    }

    @Test
    void searchReturnsCachedResponseWhenAvailable() throws Exception {
        DictionaryWordResponse cachedResponse = new DictionaryWordResponse(
                "abandon",
                "/uh-BAN-duhn/",
                null,
                List.of()
        );
        DictionaryProvider provider = mock(DictionaryProvider.class);
        StringRedisTemplate redisTemplate = mockRedisTemplate(objectMapper.writeValueAsString(cachedResponse));
        DictionaryService service = new DictionaryService(provider, redisTemplate, objectMapper);

        DictionaryWordResponse response = service.search("abandon");

        assertThat(response).isEqualTo(cachedResponse);
        verifyNoInteractions(provider);
    }

    @Test
    void searchRejectsInvalidWord() {
        DictionaryProvider provider = mock(DictionaryProvider.class);
        StringRedisTemplate redisTemplate = mockRedisTemplate(null);
        DictionaryService service = new DictionaryService(provider, redisTemplate, objectMapper);

        assertThatThrownBy(() -> service.search("abandon123"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT);
        verifyNoInteractions(provider);
    }

    @SuppressWarnings("unchecked")
    private StringRedisTemplate mockRedisTemplate(String cachedValue) {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(cachedValue);
        return redisTemplate;
    }
}
