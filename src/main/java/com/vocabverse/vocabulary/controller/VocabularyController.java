package com.vocabverse.vocabulary.controller;

import com.vocabverse.common.response.ApiResponse;
import com.vocabverse.vocabulary.dto.request.CreateVocabularyRequest;
import com.vocabverse.vocabulary.dto.request.UpdateVocabularyRequest;
import com.vocabverse.vocabulary.dto.response.VocabularyPageResponse;
import com.vocabverse.vocabulary.dto.response.VocabularyResponse;
import com.vocabverse.vocabulary.service.VocabularyService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vocabularies")
public class VocabularyController {

    private final VocabularyService vocabularyService;

    @PostMapping
    public ApiResponse<VocabularyResponse> createVocabulary(
            @Valid @RequestBody CreateVocabularyRequest request
    ) {
        return ApiResponse.success(vocabularyService.createVocabulary(request));
    }

    @GetMapping
    public ApiResponse<VocabularyPageResponse> getMyVocabularies(
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ApiResponse.success(vocabularyService.getMyVocabularies(pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<VocabularyResponse> getVocabularyDetail(@PathVariable UUID id) {
        return ApiResponse.success(vocabularyService.getVocabularyDetail(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<VocabularyResponse> updateVocabulary(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateVocabularyRequest request
    ) {
        return ApiResponse.success(vocabularyService.updateVocabulary(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteVocabulary(@PathVariable UUID id) {
        vocabularyService.deleteVocabulary(id);
        return ApiResponse.success("Delete vocabulary successfully", null);
    }
}
