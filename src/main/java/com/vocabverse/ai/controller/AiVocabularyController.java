package com.vocabverse.ai.controller;

import com.vocabverse.ai.dto.request.NormalizeBulkVocabularyRequest;
import com.vocabverse.ai.dto.request.NormalizeVocabularyRequest;
import com.vocabverse.ai.dto.response.NormalizeBulkVocabularyResponse;
import com.vocabverse.ai.dto.response.NormalizeVocabularyResponse;
import com.vocabverse.ai.service.AiVocabularyService;
import com.vocabverse.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/vocabulary")
public class AiVocabularyController {

    private final AiVocabularyService aiVocabularyService;

    @PostMapping("/normalize")
    public ApiResponse<NormalizeVocabularyResponse> normalize(
            @Valid @RequestBody NormalizeVocabularyRequest request
    ) {
        return ApiResponse.success(aiVocabularyService.normalize(request));
    }

    @PostMapping("/normalize-bulk")
    public ApiResponse<NormalizeBulkVocabularyResponse> normalizeBulk(
            @Valid @RequestBody NormalizeBulkVocabularyRequest request
    ) {
        return ApiResponse.success(aiVocabularyService.normalizeBulk(request));
    }
}
