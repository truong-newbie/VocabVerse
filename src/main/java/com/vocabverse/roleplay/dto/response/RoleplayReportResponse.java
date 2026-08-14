package com.vocabverse.roleplay.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RoleplayReportResponse(
        UUID id,
        UUID sessionId,
        String summary,
        List<String> strengths,
        List<String> weaknesses,
        List<String> suggestedVocabulary,
        String grammarFeedback,
        int overallScore,
        Integer grammarScore,
        Integer vocabularyScore,
        Integer relevanceScore,
        Integer fluencyScore,
        Integer interactionScore,
        LocalDateTime createdAt
) {
}
