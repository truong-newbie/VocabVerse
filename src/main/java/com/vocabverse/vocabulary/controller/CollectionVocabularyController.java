package com.vocabverse.vocabulary.controller;

import com.vocabverse.common.response.ApiResponse;
import com.vocabverse.vocabulary.dto.response.VocabularyPageResponse;
import com.vocabverse.vocabulary.dto.response.VocabularyResponse;
import com.vocabverse.vocabulary.service.VocabularyService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/collections/{collectionId}/vocabularies")
public class CollectionVocabularyController {

    private final VocabularyService vocabularyService;

    @PostMapping("/{vocabularyId}")
    public ApiResponse<VocabularyResponse> addVocabularyToCollection(
            @PathVariable UUID collectionId,
            @PathVariable UUID vocabularyId
    ) {
        return ApiResponse.success(vocabularyService.addVocabularyToCollection(collectionId, vocabularyId));
    }

    @DeleteMapping("/{vocabularyId}")
    public ApiResponse<Void> removeVocabularyFromCollection(
            @PathVariable UUID collectionId,
            @PathVariable UUID vocabularyId
    ) {
        vocabularyService.removeVocabularyFromCollection(collectionId, vocabularyId);
        return ApiResponse.success("Remove vocabulary from collection successfully", null);
    }

    @GetMapping
    public ApiResponse<VocabularyPageResponse> getCollectionVocabularies(
            @PathVariable UUID collectionId,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ApiResponse.success(vocabularyService.getCollectionVocabularies(collectionId, pageable));
    }
}
