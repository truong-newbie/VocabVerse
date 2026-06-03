package com.vocabverse.ai.client;

import com.vocabverse.ai.dto.response.NormalizeVocabularyResponse;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LocalFallbackAiClient implements AiClient {

    @Override
    public NormalizeVocabularyResponse normalizeVocabulary(String rawText, String prompt) {
        String term = rawText.trim();
        return new NormalizeVocabularyResponse(
                term,
                "Suggested meaning for \"" + term + "\". Configure an AI provider for enriched output.",
                "",
                "",
                "Example usage for \"" + term + "\" should be reviewed before saving.",
                "",
                List.of(),
                List.of(),
                "MEDIUM",
                "Fallback normalization was used because no external AI provider is configured."
        );
    }
}
