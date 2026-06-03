package com.vocabverse.learning.quiz.controller;

import com.vocabverse.common.response.ApiResponse;
import com.vocabverse.learning.quiz.dto.request.CreateQuizSessionRequest;
import com.vocabverse.learning.quiz.dto.request.SubmitQuizAnswerRequest;
import com.vocabverse.learning.quiz.dto.response.QuizAnswerResponse;
import com.vocabverse.learning.quiz.dto.response.QuizQuestionResponse;
import com.vocabverse.learning.quiz.dto.response.QuizSessionResponse;
import com.vocabverse.learning.quiz.service.QuizService;
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
@RequestMapping("/quizzes/sessions")
public class QuizController {

    private final QuizService quizService;

    @PostMapping
    public ApiResponse<QuizSessionResponse> createSession(
            @Valid @RequestBody CreateQuizSessionRequest request
    ) {
        return ApiResponse.success(quizService.createSession(request));
    }

    @GetMapping("/{sessionId}")
    public ApiResponse<QuizSessionResponse> getSessionDetail(@PathVariable UUID sessionId) {
        return ApiResponse.success(quizService.getSessionDetail(sessionId));
    }

    @GetMapping("/{sessionId}/questions")
    public ApiResponse<List<QuizQuestionResponse>> getSessionQuestions(@PathVariable UUID sessionId) {
        return ApiResponse.success(quizService.getSessionQuestions(sessionId));
    }

    @PostMapping("/{sessionId}/questions/{questionId}/answer")
    public ApiResponse<QuizAnswerResponse> submitAnswer(
            @PathVariable UUID sessionId,
            @PathVariable UUID questionId,
            @Valid @RequestBody SubmitQuizAnswerRequest request
    ) {
        return ApiResponse.success(quizService.submitAnswer(sessionId, questionId, request));
    }
}
