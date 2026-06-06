package com.vocabverse.admin.controller;

import com.vocabverse.admin.dto.request.AdminHidePublicCollectionRequest;
import com.vocabverse.admin.dto.request.AdminUpdateCollectionVisibilityRequest;
import com.vocabverse.admin.dto.response.AdminCollectionResponse;
import com.vocabverse.admin.dto.response.AdminPageResponse;
import com.vocabverse.admin.service.AdminCollectionService;
import com.vocabverse.collection.enums.CollectionVisibility;
import com.vocabverse.common.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCollectionController {

    private final AdminCollectionService adminCollectionService;

    @GetMapping("/collections")
    public ApiResponse<AdminPageResponse<AdminCollectionResponse>> listCollections(
            @RequestParam(required = false) CollectionVisibility visibility,
            @PageableDefault(page = 0, size = 20) Pageable pageable
    ) {
        return ApiResponse.success(adminCollectionService.listCollections(visibility, pageable));
    }

    @GetMapping("/collections/{collectionId}")
    public ApiResponse<AdminCollectionResponse> getCollection(@PathVariable UUID collectionId) {
        return ApiResponse.success(adminCollectionService.getCollection(collectionId));
    }

    @PutMapping("/collections/{collectionId}/visibility")
    public ApiResponse<AdminCollectionResponse> updateVisibility(
            @PathVariable UUID collectionId,
            @Valid @RequestBody AdminUpdateCollectionVisibilityRequest request
    ) {
        return ApiResponse.success(adminCollectionService.updateVisibility(collectionId, request));
    }

    @DeleteMapping("/collections/{collectionId}")
    public ApiResponse<Void> deleteCollection(@PathVariable UUID collectionId) {
        adminCollectionService.deleteCollection(collectionId);
        return ApiResponse.success("Delete collection successfully", null);
    }

    @GetMapping("/public-collections")
    public ApiResponse<AdminPageResponse<AdminCollectionResponse>> listPublicCollections(
            @PageableDefault(page = 0, size = 20) Pageable pageable
    ) {
        return ApiResponse.success(adminCollectionService.listPublicCollections(pageable));
    }

    @PutMapping("/public-collections/{collectionId}/hide")
    public ApiResponse<AdminCollectionResponse> hidePublicCollection(
            @PathVariable UUID collectionId,
            @Valid @RequestBody AdminHidePublicCollectionRequest request
    ) {
        return ApiResponse.success(adminCollectionService.hidePublicCollection(collectionId, request));
    }
}
