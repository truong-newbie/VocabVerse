package com.vocabverse.learning.flashcard.controller;

import com.vocabverse.common.response.ApiResponse;
import com.vocabverse.learning.flashcard.dto.request.CreateFlashcardSessionRequest;
import com.vocabverse.learning.flashcard.dto.request.SubmitFlashcardAnswerRequest;
import com.vocabverse.learning.flashcard.dto.response.FlashcardAnswerResponse;
import com.vocabverse.learning.flashcard.dto.response.FlashcardCardResponse;
import com.vocabverse.learning.flashcard.dto.response.FlashcardSessionResponse;
import com.vocabverse.learning.flashcard.service.FlashcardService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/flashcards/sessions")
public class FlashcardController {

    private final FlashcardService flashcardService;

    @PostMapping
    public ApiResponse<FlashcardSessionResponse> createSession(
            @Valid @RequestBody CreateFlashcardSessionRequest request
    ) {
        return ApiResponse.success(flashcardService.createSession(request));
    }

    @GetMapping("/{sessionId}")
    public ApiResponse<FlashcardSessionResponse> getSessionDetail(@PathVariable UUID sessionId) {
        return ApiResponse.success(flashcardService.getSessionDetail(sessionId));
    }

    @GetMapping("/{sessionId}/cards")
    public ApiResponse<List<FlashcardCardResponse>> getSessionCards(@PathVariable UUID sessionId) {
        return ApiResponse.success(flashcardService.getSessionCards(sessionId));
    }

    @PostMapping("/{sessionId}/cards/{vocabularyId}/answer")
    public ApiResponse<FlashcardAnswerResponse> submitAnswer(
            @PathVariable UUID sessionId,
            @PathVariable UUID vocabularyId,
            @Valid @RequestBody SubmitFlashcardAnswerRequest request
    ) {
        return ApiResponse.success(flashcardService.submitAnswer(sessionId, vocabularyId, request));
    }
}
