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
                - Keep the opening message concise.
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
                Generate a final roleplay report.
                Return JSON only with this schema:
                {
                  "summary": "...",
                  "strengths": [],
                  "weaknesses": [],
                  "suggestedVocabulary": [],
                  "grammarFeedback": "...",
                  "overallScore": 0
                }

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
