package com.vocabverse.ai.prompt;

import org.springframework.stereotype.Component;

@Component
public class VocabularyNormalizePromptBuilder {

    public String build(String rawText) {
        return """
                Normalize this English vocabulary input into one strict JSON object.

                Output rules:
                Return JSON only.
                Do not use markdown.
                Do not wrap output in a code block.
                Do not include explanation outside JSON.
                Do not include conversational text.

                Required JSON structure:
                {
                  "term": "",
                  "meaning": "",
                  "vietnameseMeaning": "",
                  "pronunciation": "",
                  "partOfSpeech": "",
                  "exampleSentence": "",
                  "synonyms": [],
                  "antonyms": [],
                  "difficulty": "",
                  "aiExplanation": ""
                }

                Field rules:
                term: normalized English headword or phrase.
                meaning: concise English learner-friendly definition.
                vietnameseMeaning: clear, natural Vietnamese meaning for learners.
                pronunciation: IPA pronunciation, for example "/əˈbændən/".
                partOfSpeech: common part of speech such as noun, verb, adjective, adverb, phrase.
                exampleSentence: one natural, simple, educational English sentence.
                synonyms: JSON array of short English synonyms; empty array if none.
                antonyms: JSON array of short English antonyms; empty array if none.
                difficulty: exactly one of BEGINNER, INTERMEDIATE, ADVANCED based on common English usage.
                aiExplanation: one concise sentence explaining normalization choices.

                Input:
                %s
                """.formatted(rawText);
    }
}
