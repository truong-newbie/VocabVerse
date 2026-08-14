package com.vocabverse.roleplay.dto.response;

import java.util.List;

public record RoleplayAiReport(
        String summary,
        List<String> strengths,
        List<String> weaknesses,
        List<String> suggestedVocabulary,
        String grammarFeedback,
        int overallScore,
        int grammarScore,
        int vocabularyScore,
        int relevanceScore,
        int fluencyScore,
        int interactionScore
) {
}
