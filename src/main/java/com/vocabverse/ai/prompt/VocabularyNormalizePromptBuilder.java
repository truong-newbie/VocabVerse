package com.vocabverse.ai.prompt;

import org.springframework.stereotype.Component;

@Component
public class VocabularyNormalizePromptBuilder {

    public String build(String rawText) {
        return """
                Normalize this English vocabulary input and return only structured JSON.
                Required JSON fields:
                term, meaning, pronunciation, partOfSpeech, exampleSentence,
                vietnameseMeaning, synonyms, antonyms, difficulty, aiExplanation.
                Difficulty must be one of EASY, MEDIUM, HARD.
                Input: %s
                """.formatted(rawText);
    }
}
