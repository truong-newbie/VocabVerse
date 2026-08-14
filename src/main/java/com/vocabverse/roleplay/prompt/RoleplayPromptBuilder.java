package com.vocabverse.roleplay.prompt;

import com.vocabverse.roleplay.entity.RoleplayMessageEntity;
import com.vocabverse.roleplay.entity.RoleplaySessionEntity;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RoleplayPromptBuilder {

    public String buildScenarioPrompt(String topic, String difficulty, String persona) {
        return """
                You are creating an English roleplay scenario.
                Return JSON only with this schema:
                {"scenario": "...", "firstMessage": "..."}

                Rules:
                - Topic: %s
                - Difficulty: %s
                - Persona: %s
                - Stay in character.
                - Use English only.
                - Keep the opening message concise.
                - Invite the learner to reply in English.
                """.formatted(topic, difficulty, persona);
    }

    public String buildReplyPrompt(RoleplaySessionEntity session, List<RoleplayMessageEntity> messages, String userMessage) {
        return """
                You are acting as the roleplay persona.
                Return JSON only with this schema:
                {
                  "reply": "...",
                  "correction": {
                    "original": "...",
                    "corrected": "...",
                    "betterExpression": "...",
                    "explanation": "..."
                  }
                }

                Session:
                - Topic: %s
                - Difficulty: %s
                - Persona: %s
                - Scenario: %s

                Recent conversation:
                %s

                User message:
                %s

                Rules:
                - Reply in English only.
                - Stay in character as the persona.
                - Keep the reply short enough for speaking practice.
                - Correction must evaluate the user's English, not the roleplay facts.
                - If the user made no meaningful mistake, keep corrected and betterExpression natural but close to the original.
                """.formatted(
                session.getTopic(),
                session.getDifficulty(),
                session.getPersona(),
                session.getScenario(),
                formatMessages(messages),
                userMessage
        );
    }

    public String buildReportPrompt(RoleplaySessionEntity session, List<RoleplayMessageEntity> messages) {
        return """
                Generate a final English-learning roleplay report.
                Return JSON only with this schema:
                {
                  "summary": "...",
                  "strengths": [],
                  "weaknesses": [],
                  "suggestedVocabulary": [],
                  "grammarFeedback": "...",
                  "overallScore": 0,
                  "grammarScore": 0,
                  "vocabularyScore": 0,
                  "relevanceScore": 0,
                  "fluencyScore": 0,
                  "interactionScore": 0
                }

                Scoring rubric, total 100:
                - grammarScore: 0-25 points
                - vocabularyScore: 0-20 points
                - relevanceScore: 0-20 points
                - fluencyScore: 0-20 points
                - interactionScore: 0-15 points
                - overallScore must equal the sum of the five rubric scores.

                Grade only the learner's English messages. Be fair but not inflated.

                Session:
                - Topic: %s
                - Difficulty: %s
                - Persona: %s

                Conversation:
                %s
                """.formatted(session.getTopic(), session.getDifficulty(), session.getPersona(), formatMessages(messages));
    }

    private String formatMessages(List<RoleplayMessageEntity> messages) {
        if (messages.isEmpty()) {
            return "(no messages yet)";
        }
        return messages.stream()
                .map(message -> message.getSender() + ": " + message.getContent())
                .reduce((left, right) -> left + "\n" + right)
                .orElse("(no messages yet)");
    }
}
