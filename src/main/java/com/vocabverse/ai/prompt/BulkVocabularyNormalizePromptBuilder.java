package com.vocabverse.ai.prompt;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class BulkVocabularyNormalizePromptBuilder {

    public String build(List<String> terms) {
        return """
                Normalize these English vocabulary terms into JSON.

                Rules:
                Return JSON only.
                Do not use markdown.
                Do not include explanation.
                Do not wrap output in a code block.
                Return exactly one JSON array.
                Keep meanings concise and learner-friendly.
                Use Vietnamese for vietnameseMeaning.
                If pronunciation is unknown, return an empty string.
                If partOfSpeech is uncertain, return an empty string.
                If exampleSentence is unavailable, create a simple natural English sentence.
                Always include note as an empty string unless there is a useful warning.

                Expected structure:
                [
                  {
                    "term": "",
                    "meaning": "",
                    "vietnameseMeaning": "",
                    "pronunciation": "",
                    "partOfSpeech": "",
                    "exampleSentence": "",
                    "note": ""
                  }
                ]

                Terms:
                %s
                """.formatted(String.join("\n", terms));
    }
}
