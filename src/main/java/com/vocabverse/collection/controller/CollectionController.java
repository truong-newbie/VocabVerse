package com.vocabverse.collection.controller;

import com.vocabverse.collection.dto.request.CreateCollectionRequest;
import com.vocabverse.collection.dto.request.UpdateCollectionRequest;
import com.vocabverse.collection.dto.response.CollectionPageResponse;
import com.vocabverse.collection.dto.response.CollectionResponse;
import com.vocabverse.collection.service.CollectionService;
import com.vocabverse.common.response.ApiResponse;
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
@RequestMapping("/collections")
public class CollectionController {

    private final CollectionService collectionService;

    @PostMapping
    public ApiResponse<CollectionResponse> createCollection(
            @Valid @RequestBody CreateCollectionRequest request
    ) {
        return ApiResponse.success(collectionService.createCollection(request));
    }

    @GetMapping
    public ApiResponse<CollectionPageResponse> getMyCollections(
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ApiResponse.success(collectionService.getMyCollections(pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<CollectionResponse> getCollectionDetail(@PathVariable UUID id) {
        return ApiResponse.success(collectionService.getCollectionDetail(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<CollectionResponse> updateCollection(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCollectionRequest request
    ) {
        return ApiResponse.success(collectionService.updateCollection(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCollection(@PathVariable UUID id) {
        collectionService.deleteCollection(id);
        return ApiResponse.success("Delete collection successfully", null);
    }
}
