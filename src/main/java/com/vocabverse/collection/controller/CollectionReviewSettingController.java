package com.vocabverse.collection.controller;

import com.vocabverse.collection.dto.request.UpdateCollectionReviewSettingRequest;
import com.vocabverse.collection.dto.response.CollectionReviewSettingResponse;
import com.vocabverse.collection.service.CollectionReviewSettingService;
import com.vocabverse.common.response.ApiResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/collections/{collectionId}/review-settings")
public class CollectionReviewSettingController {

    private final CollectionReviewSettingService collectionReviewSettingService;

    @GetMapping
    public ApiResponse<CollectionReviewSettingResponse> getSettings(@PathVariable UUID collectionId) {
        return ApiResponse.success(collectionReviewSettingService.getSettings(collectionId));
    }

    @PutMapping
    public ApiResponse<CollectionReviewSettingResponse> updateSettings(
            @PathVariable UUID collectionId,
            @RequestBody UpdateCollectionReviewSettingRequest request
    ) {
        return ApiResponse.success(collectionReviewSettingService.updateSettings(collectionId, request));
    }

    @PostMapping("/disable")
    public ApiResponse<CollectionReviewSettingResponse> disableSettings(@PathVariable UUID collectionId) {
        return ApiResponse.success(collectionReviewSettingService.disableSettings(collectionId));
    }

    @PostMapping("/reset")
    public ApiResponse<CollectionReviewSettingResponse> resetSchedule(@PathVariable UUID collectionId) {
        return ApiResponse.success(collectionReviewSettingService.resetSchedule(collectionId));
    }
}
