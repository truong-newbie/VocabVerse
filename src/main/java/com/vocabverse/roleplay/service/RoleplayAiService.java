package com.vocabverse.roleplay.service;

import com.vocabverse.roleplay.dto.response.RoleplayAiReply;
import com.vocabverse.roleplay.dto.response.RoleplayAiReport;
import com.vocabverse.roleplay.dto.response.RoleplayAiScenario;
import com.vocabverse.roleplay.entity.RoleplayCorrection;
import com.vocabverse.roleplay.entity.RoleplayMessageEntity;
import com.vocabverse.roleplay.entity.RoleplaySessionEntity;
import com.vocabverse.roleplay.prompt.RoleplayPromptBuilder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleplayAiService {

    private final RoleplayPromptBuilder promptBuilder;

    public RoleplayAiScenario startSession(String topic, String difficulty, String persona) {
        promptBuilder.buildScenarioPrompt(topic, difficulty, persona);
        String scenario = "You are practicing a conversation about " + topic + " with " + persona + ".";
        String firstMessage = switch (difficulty) {
            case "EASY" -> "Hi, let's talk about " + topic + ". What would you like to say first?";
            case "HARD" -> "Let's begin. Explain your thoughts about " + topic + " in detail.";
            default -> "Hi, let's start our conversation about " + topic + ".";
        };
        return new RoleplayAiScenario(scenario, firstMessage);
    }

    public RoleplayAiReply reply(RoleplaySessionEntity session, List<RoleplayMessageEntity> messages, String userMessage) {
        promptBuilder.buildReplyPrompt(session, messages, userMessage);
        String reply = buildReply(session, userMessage);
        RoleplayCorrection correction = buildCorrection(userMessage);
        return new RoleplayAiReply(reply, correction);
    }

    public RoleplayAiReport report(RoleplaySessionEntity session, List<RoleplayMessageEntity> messages) {
        promptBuilder.buildReportPrompt(session, messages);
        long userMessageCount = messages.stream()
                .filter(message -> message.getSender().name().equals("USER"))
                .count();
        int score = Math.min(95, 65 + (int) userMessageCount * 5);

        return new RoleplayAiReport(
                "You completed a roleplay session about " + session.getTopic() + ".",
                List.of("Stayed on topic", "Practiced conversation flow"),
                List.of("Add more detail in answers", "Review grammar before speaking faster"),
                List.of(session.getTopic(), "Could you clarify?", "That sounds useful."),
                "Focus on complete sentences and natural connectors.",
                score
        );
    }

    private String buildReply(RoleplaySessionEntity session, String userMessage) {
        if (userMessage.endsWith("?")) {
            return "Good question. In this " + session.getTopic() + " situation, I would say yes. What do you think?";
        }
        return "I understand. Can you tell me more about that in the context of " + session.getTopic() + "?";
    }

    private RoleplayCorrection buildCorrection(String userMessage) {
        String trimmed = userMessage.trim();
        String corrected = trimmed.isEmpty()
                ? trimmed
                : Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
        if (!corrected.endsWith(".") && !corrected.endsWith("?") && !corrected.endsWith("!")) {
            corrected = corrected + ".";
        }

        return new RoleplayCorrection(
                userMessage,
                corrected,
                corrected,
                "Use a complete sentence with clear punctuation."
        );
    }
}
