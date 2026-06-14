package com.vocabverse.dictionary.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.dictionary.dto.response.DictionaryWordResponse;
import com.vocabverse.dictionary.mapper.DictionaryMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class DictionaryProviderImpl implements DictionaryProvider {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final ObjectMapper objectMapper;
    private final DictionaryMapper dictionaryMapper;
    private final HttpClient httpClient;

    @Value("${dictionary.api.base-url:}")
    private String baseUrl;

    public DictionaryProviderImpl(ObjectMapper objectMapper, DictionaryMapper dictionaryMapper) {
        this.objectMapper = objectMapper;
        this.dictionaryMapper = dictionaryMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
    }

    @Override
    public DictionaryWordResponse search(String word) {
        String requestId = UUID.randomUUID().toString();
        long startedAt = System.nanoTime();

        try {
            HttpRequest request = HttpRequest.newBuilder(buildUri(word))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long durationMs = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
            log.info("Dictionary search provider=HTTP requestId={} word={} durationMs={} status={}",
                    requestId,
                    word,
                    durationMs,
                    response.statusCode());

            if (response.statusCode() == 404) {
                throw new BusinessException(ErrorCode.DICTIONARY_WORD_NOT_FOUND);
            }
            if (response.statusCode() == 408 || response.statusCode() == 504) {
                throw new BusinessException(ErrorCode.DICTIONARY_TIMEOUT);
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException(ErrorCode.DICTIONARY_PROVIDER_UNAVAILABLE);
            }

            JsonNode root = objectMapper.readTree(response.body());
            return dictionaryMapper.toResponse(root);
        } catch (BusinessException exception) {
            log.warn("Dictionary search failed requestId={} word={} errorCode={}",
                    requestId,
                    word,
                    exception.getErrorCode().getCode());
            throw exception;
        } catch (HttpTimeoutException exception) {
            log.warn("Dictionary search timeout requestId={} word={}", requestId, word);
            throw new BusinessException(
                    ErrorCode.DICTIONARY_TIMEOUT,
                    ErrorCode.DICTIONARY_TIMEOUT.getMessage(),
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(
                    ErrorCode.DICTIONARY_PROVIDER_UNAVAILABLE,
                    ErrorCode.DICTIONARY_PROVIDER_UNAVAILABLE.getMessage(),
                    exception
            );
        } catch (JsonProcessingException exception) {
            throw new BusinessException(
                    ErrorCode.DICTIONARY_INVALID_RESPONSE,
                    ErrorCode.DICTIONARY_INVALID_RESPONSE.getMessage(),
                    exception
            );
        } catch (Exception exception) {
            log.warn("Dictionary search failed requestId={} word={} errorCode={}",
                    requestId,
                    word,
                    ErrorCode.DICTIONARY_PROVIDER_UNAVAILABLE.getCode());
            throw new BusinessException(
                    ErrorCode.DICTIONARY_PROVIDER_UNAVAILABLE,
                    ErrorCode.DICTIONARY_PROVIDER_UNAVAILABLE.getMessage(),
                    exception
            );
        }
    }

    private URI buildUri(String word) {
        if (!StringUtils.hasText(baseUrl)) {
            throw new BusinessException(ErrorCode.DICTIONARY_PROVIDER_UNAVAILABLE);
        }

        String encodedWord = URLEncoder.encode(word, StandardCharsets.UTF_8).replace("+", "%20");
        String url = baseUrl.trim();
        if (url.contains("{word}")) {
            return URI.create(url.replace("{word}", encodedWord));
        }
        if (url.endsWith("/entries")) {
            return URI.create(url + "/en/" + encodedWord);
        }
        String separator = url.endsWith("/") ? "" : "/";
        return URI.create(url + separator + encodedWord);
    }
}
