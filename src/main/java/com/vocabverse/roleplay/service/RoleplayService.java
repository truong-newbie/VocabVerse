package com.vocabverse.roleplay.service;

import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.roleplay.dto.request.CreateRoleplaySessionRequest;
import com.vocabverse.roleplay.dto.request.SendRoleplayMessageRequest;
import com.vocabverse.roleplay.dto.response.RoleplayAiReply;
import com.vocabverse.roleplay.dto.response.RoleplayAiReport;
import com.vocabverse.roleplay.dto.response.RoleplayAiScenario;
import com.vocabverse.roleplay.dto.response.RoleplayMessageResponse;
import com.vocabverse.roleplay.dto.response.RoleplayReportResponse;
import com.vocabverse.roleplay.dto.response.RoleplaySessionPageResponse;
import com.vocabverse.roleplay.dto.response.RoleplaySessionResponse;
import com.vocabverse.roleplay.entity.RoleplayMessageEntity;
import com.vocabverse.roleplay.entity.RoleplayReportEntity;
import com.vocabverse.roleplay.entity.RoleplaySessionEntity;
import com.vocabverse.roleplay.enums.RoleplayMessageSender;
import com.vocabverse.roleplay.enums.RoleplaySessionStatus;
import com.vocabverse.roleplay.mapper.RoleplayMapper;
import com.vocabverse.roleplay.repository.RoleplayMessageRepository;
import com.vocabverse.roleplay.repository.RoleplayReportRepository;
import com.vocabverse.roleplay.repository.RoleplaySessionRepository;
import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleplayService {

    private final RoleplaySessionRepository roleplaySessionRepository;
    private final RoleplayMessageRepository roleplayMessageRepository;
    private final RoleplayReportRepository roleplayReportRepository;
    private final UserRepository userRepository;
    private final RoleplayAiService roleplayAiService;
    private final RoleplayMapper roleplayMapper;

    @Transactional
    public RoleplaySessionResponse createSession(CreateRoleplaySessionRequest request) {
        UserEntity user = getCurrentUser();
        RoleplayAiScenario aiScenario = roleplayAiService.startSession(
                request.topic().trim(),
                request.difficulty().name(),
                request.persona().trim()
        );

        RoleplaySessionEntity session = roleplaySessionRepository.save(RoleplaySessionEntity.builder()
                .user(user)
                .topic(request.topic().trim())
                .difficulty(request.difficulty())
                .persona(request.persona().trim())
                .scenario(aiScenario.scenario())
                .status(RoleplaySessionStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .build());

        RoleplayMessageEntity openingMessage = roleplayMessageRepository.save(RoleplayMessageEntity.builder()
                .session(session)
                .sender(RoleplayMessageSender.AI)
                .content(aiScenario.firstMessage())
                .build());

        return toSessionResponse(session, List.of(openingMessage), null);
    }

    @Transactional(readOnly = true)
    public RoleplaySessionResponse getSession(UUID sessionId) {
        RoleplaySessionEntity session = getOwnedSession(sessionId);
        return toSessionResponse(session);
    }

    @Transactional(readOnly = true)
    public RoleplaySessionPageResponse getSessions(Pageable pageable) {
        UUID userId = getCurrentUser().getId();
        Page<RoleplaySessionEntity> page = roleplaySessionRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable);

        return new RoleplaySessionPageResponse(
                page.map(session -> toSessionResponse(session, List.of(), null)).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Transactional
    public RoleplayMessageResponse sendMessage(UUID sessionId, SendRoleplayMessageRequest request) {
        RoleplaySessionEntity session = getOwnedSession(sessionId);
        if (session.getStatus() == RoleplaySessionStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.ROLEPLAY_SESSION_COMPLETED);
        }

        String userMessageText = request.message().trim();
        RoleplayMessageEntity userMessage = roleplayMessageRepository.save(RoleplayMessageEntity.builder()
                .session(session)
                .sender(RoleplayMessageSender.USER)
                .content(userMessageText)
                .build());

        List<RoleplayMessageEntity> messages = roleplayMessageRepository.findAllBySessionIdOrderByCreatedAtAsc(sessionId);
        RoleplayAiReply aiReply = roleplayAiService.reply(session, messages, userMessageText);

        RoleplayMessageEntity aiMessage = roleplayMessageRepository.save(RoleplayMessageEntity.builder()
                .session(session)
                .sender(RoleplayMessageSender.AI)
                .content(aiReply.reply())
                .correction(aiReply.correction())
                .build());

        return roleplayMapper.toMessageResponse(aiMessage);
    }

    @Transactional
    public RoleplayReportResponse endSession(UUID sessionId) {
        RoleplaySessionEntity session = getOwnedSession(sessionId);
        Optional<RoleplayReportEntity> existingReport = roleplayReportRepository.findBySessionId(session.getId());
        if (existingReport.isPresent()) {
            return roleplayMapper.toReportResponse(existingReport.get());
        }

        List<RoleplayMessageEntity> messages = roleplayMessageRepository.findAllBySessionIdOrderByCreatedAtAsc(session.getId());
        RoleplayAiReport aiReport = roleplayAiService.report(session, messages);

        session.setStatus(RoleplaySessionStatus.COMPLETED);
        session.setEndedAt(LocalDateTime.now());
        roleplaySessionRepository.save(session);

        RoleplayReportEntity report = roleplayReportRepository.save(RoleplayReportEntity.builder()
                .session(session)
                .summary(aiReport.summary())
                .strengths(aiReport.strengths())
                .weaknesses(aiReport.weaknesses())
                .suggestedVocabulary(aiReport.suggestedVocabulary())
                .grammarFeedback(aiReport.grammarFeedback())
                .overallScore(aiReport.overallScore())
                .build());

        return roleplayMapper.toReportResponse(report);
    }

    private RoleplaySessionResponse toSessionResponse(RoleplaySessionEntity session) {
        List<RoleplayMessageEntity> messages = roleplayMessageRepository.findAllBySessionIdOrderByCreatedAtAsc(session.getId());
        RoleplayReportResponse report = roleplayReportRepository.findBySessionId(session.getId())
                .map(roleplayMapper::toReportResponse)
                .orElse(null);
        return toSessionResponse(session, messages, report);
    }

    private RoleplaySessionResponse toSessionResponse(
            RoleplaySessionEntity session,
            List<RoleplayMessageEntity> messages,
            RoleplayReportResponse report
    ) {
        List<RoleplayMessageResponse> messageResponses = messages.stream()
                .map(roleplayMapper::toMessageResponse)
                .toList();
        return roleplayMapper.toSessionResponse(session, messageResponses, report);
    }

    private RoleplaySessionEntity getOwnedSession(UUID sessionId) {
        UUID userId = getCurrentUser().getId();
        return roleplaySessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLEPLAY_SESSION_NOT_FOUND));
    }

    private UserEntity getCurrentUser() {
        String email = getAuthenticatedEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private String getAuthenticatedEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return authentication.getName();
    }
}
