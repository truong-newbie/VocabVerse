package com.vocabverse.learning.typing.controller;

import com.vocabverse.common.response.ApiResponse;
import com.vocabverse.learning.typing.dto.request.CreateTypingSessionRequest;
import com.vocabverse.learning.typing.dto.request.SubmitTypingAnswerRequest;
import com.vocabverse.learning.typing.dto.response.TypingAnswerResponse;
import com.vocabverse.learning.typing.dto.response.TypingQuestionResponse;
import com.vocabverse.learning.typing.dto.response.TypingSessionResponse;
import com.vocabverse.learning.typing.service.TypingService;
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
@RequestMapping("/typing/sessions")
public class TypingController {

    private final TypingService typingService;

    @PostMapping
    public ApiResponse<TypingSessionResponse> createSession(
            @Valid @RequestBody CreateTypingSessionRequest request
    ) {
        return ApiResponse.success(typingService.createSession(request));
    }

    @GetMapping("/{sessionId}")
    public ApiResponse<TypingSessionResponse> getSessionDetail(@PathVariable UUID sessionId) {
        return ApiResponse.success(typingService.getSessionDetail(sessionId));
    }

    @GetMapping("/{sessionId}/questions")
    public ApiResponse<List<TypingQuestionResponse>> getSessionQuestions(@PathVariable UUID sessionId) {
        return ApiResponse.success(typingService.getSessionQuestions(sessionId));
    }

    @PostMapping("/{sessionId}/questions/{questionId}/answer")
    public ApiResponse<TypingAnswerResponse> submitAnswer(
            @PathVariable UUID sessionId,
            @PathVariable UUID questionId,
            @Valid @RequestBody SubmitTypingAnswerRequest request
    ) {
        return ApiResponse.success(typingService.submitAnswer(sessionId, questionId, request));
    }
}
