package com.vocabverse.publiccollection.controller;

import com.vocabverse.common.response.ApiResponse;
import com.vocabverse.publiccollection.dto.response.CloneCollectionResponse;
import com.vocabverse.publiccollection.dto.response.PublicCollectionPageResponse;
import com.vocabverse.publiccollection.dto.response.PublicCollectionResponse;
import com.vocabverse.publiccollection.dto.response.PublicVocabularyPageResponse;
import com.vocabverse.publiccollection.service.PublicCollectionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public/collections")
public class PublicCollectionController {

    private final PublicCollectionService publicCollectionService;

    @GetMapping
    public ApiResponse<PublicCollectionPageResponse> getPublicCollections(
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ApiResponse.success(publicCollectionService.getPublicCollections(pageable));
    }

    @GetMapping("/{collectionId}")
    public ApiResponse<PublicCollectionResponse> getPublicCollectionDetail(@PathVariable UUID collectionId) {
        return ApiResponse.success(publicCollectionService.getPublicCollectionDetail(collectionId));
    }

    @GetMapping("/{collectionId}/vocabularies")
    public ApiResponse<PublicVocabularyPageResponse> getPublicCollectionVocabularies(
            @PathVariable UUID collectionId,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ApiResponse.success(publicCollectionService.getPublicCollectionVocabularies(collectionId, pageable));
    }

    @PostMapping("/{collectionId}/clone")
    public ApiResponse<CloneCollectionResponse> clonePublicCollection(@PathVariable UUID collectionId) {
        return ApiResponse.success(publicCollectionService.clonePublicCollection(collectionId));
    }
}
