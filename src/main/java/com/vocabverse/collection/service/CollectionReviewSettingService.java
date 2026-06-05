package com.vocabverse.collection.service;

import com.vocabverse.collection.dto.request.UpdateCollectionReviewSettingRequest;
import com.vocabverse.collection.dto.response.CollectionReviewSettingResponse;
import com.vocabverse.collection.entity.CollectionEntity;
import com.vocabverse.collection.entity.CollectionReviewSettingEntity;
import com.vocabverse.collection.repository.CollectionRepository;
import com.vocabverse.collection.repository.CollectionReviewSettingRepository;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.learning.progress.entity.LearningProgressEntity;
import com.vocabverse.learning.progress.entity.LearningStatus;
import com.vocabverse.learning.progress.repository.LearningProgressRepository;
import com.vocabverse.learning.progress.service.LearningProgressService;
import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.user.repository.UserRepository;
import com.vocabverse.vocabulary.entity.VocabularyEntity;
import com.vocabverse.vocabulary.repository.CollectionVocabularyRepository;
import com.vocabverse.vocabulary.repository.VocabularyRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CollectionReviewSettingService {

    private static final BigDecimal DEFAULT_EASE_FACTOR = BigDecimal.valueOf(2.50);
    private static final String DEFAULT_TIMEZONE = "Asia/Ho_Chi_Minh";
    private static final LocalTime DEFAULT_REMINDER_TIME = LocalTime.of(8, 0);

    private final CollectionRepository collectionRepository;
    private final CollectionReviewSettingRepository collectionReviewSettingRepository;
    private final CollectionVocabularyRepository collectionVocabularyRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final VocabularyRepository vocabularyRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CollectionReviewSettingResponse getSettings(UUID collectionId) {
        UserEntity user = getCurrentUser();
        CollectionEntity collection = getOwnedCollection(collectionId, user.getId());
        return toResponse(findOrDefault(user, collection));
    }

    @Transactional
    public CollectionReviewSettingResponse updateSettings(
            UUID collectionId,
            UpdateCollectionReviewSettingRequest request
    ) {
        UserEntity user = getCurrentUser();
        CollectionEntity collection = getOwnedCollection(collectionId, user.getId());
        CollectionReviewSettingEntity setting = findOrDefault(user, collection);

        if (request.enabled() != null) {
            setting.setEnabled(request.enabled());
        }
        if (request.emailEnabled() != null) {
            setting.setEmailEnabled(request.emailEnabled());
        }
        if (request.intervals() != null) {
            setting.setIntervalsJson(validateIntervals(request.intervals()));
        }
        if (request.reminderTime() != null) {
            setting.setReminderTime(request.reminderTime());
        }
        if (request.timezone() != null) {
            setting.setTimezone(validateTimezone(request.timezone()));
        }

        return toResponse(collectionReviewSettingRepository.save(setting));
    }

    @Transactional
    public CollectionReviewSettingResponse disableSettings(UUID collectionId) {
        UserEntity user = getCurrentUser();
        CollectionEntity collection = getOwnedCollection(collectionId, user.getId());
        CollectionReviewSettingEntity setting = findOrDefault(user, collection);
        setting.setEnabled(false);
        setting.setEmailEnabled(false);
        return toResponse(collectionReviewSettingRepository.save(setting));
    }

    @Transactional
    public CollectionReviewSettingResponse resetSchedule(UUID collectionId) {
        UserEntity user = getCurrentUser();
        CollectionEntity collection = getOwnedCollection(collectionId, user.getId());
        CollectionReviewSettingEntity setting = findOrDefault(user, collection);
        if (!setting.isEnabled()) {
            throw new BusinessException(ErrorCode.REVIEW_SETTINGS_DISABLED);
        }

        try {
            List<UUID> vocabularyIds = collectionVocabularyRepository.findVocabularyIdsByOwnedCollectionId(
                    user.getId(),
                    collectionId
            );
            if (!vocabularyIds.isEmpty()) {
                resetProgressRows(user, vocabularyIds, firstInterval(setting));
            }
            setting.setLastResetAt(LocalDateTime.now());
            return toResponse(collectionReviewSettingRepository.save(setting));
        } catch (BusinessException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new BusinessException(
                    ErrorCode.COLLECTION_REVIEW_RESET_FAILED,
                    ErrorCode.COLLECTION_REVIEW_RESET_FAILED.getMessage(),
                    exception
            );
        }
    }

    private void resetProgressRows(UserEntity user, List<UUID> vocabularyIds, int firstIntervalDays) {
        Map<UUID, LearningProgressEntity> existingProgress = learningProgressRepository
                .findAllByUserIdAndVocabularyIdIn(user.getId(), vocabularyIds)
                .stream()
                .collect(Collectors.toMap(progress -> progress.getVocabulary().getId(), Function.identity()));
        Map<UUID, VocabularyEntity> vocabularies = vocabularyRepository.findAllById(vocabularyIds)
                .stream()
                .collect(Collectors.toMap(VocabularyEntity::getId, Function.identity()));

        List<LearningProgressEntity> progressRows = new ArrayList<>();
        LocalDateTime nextReviewAt = LocalDateTime.now().plusDays(firstIntervalDays);
        for (UUID vocabularyId : vocabularyIds) {
            VocabularyEntity vocabulary = vocabularies.get(vocabularyId);
            if (vocabulary == null || vocabulary.getDeletedAt() != null) {
                continue;
            }
            LearningProgressEntity progress = existingProgress.get(vocabularyId);
            if (progress == null) {
                progress = LearningProgressEntity.builder()
                        .user(user)
                        .vocabulary(vocabulary)
                        .easeFactor(DEFAULT_EASE_FACTOR)
                        .build();
            }
            progress.setStatus(LearningStatus.NEW);
            progress.setRepetitionCount(0);
            progress.setEaseFactor(DEFAULT_EASE_FACTOR);
            progress.setLastReviewedAt(null);
            progress.setNextReviewAt(nextReviewAt);
            progressRows.add(progress);
        }
        learningProgressRepository.saveAll(progressRows);
    }

    private CollectionReviewSettingEntity findOrDefault(UserEntity user, CollectionEntity collection) {
        return collectionReviewSettingRepository.findByUserIdAndCollectionId(user.getId(), collection.getId())
                .orElseGet(() -> collectionReviewSettingRepository.save(CollectionReviewSettingEntity.builder()
                        .user(user)
                        .collection(collection)
                        .enabled(true)
                        .emailEnabled(true)
                        .intervalsJson(LearningProgressService.DEFAULT_REVIEW_INTERVAL_DAYS)
                        .reminderTime(DEFAULT_REMINDER_TIME)
                        .timezone(DEFAULT_TIMEZONE)
                        .build()));
    }

    private CollectionEntity getOwnedCollection(UUID collectionId, UUID userId) {
        return collectionRepository.findByIdAndOwnerIdAndDeletedAtIsNull(collectionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COLLECTION_NOT_FOUND));
    }

    private List<Integer> validateIntervals(List<Integer> intervals) {
        if (intervals == null || intervals.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REVIEW_INTERVALS);
        }
        for (Integer interval : intervals) {
            if (interval == null || interval < 1 || interval > 3650) {
                throw new BusinessException(ErrorCode.INVALID_REVIEW_INTERVALS);
            }
        }
        return List.copyOf(intervals);
    }

    private String validateTimezone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return DEFAULT_TIMEZONE;
        }
        try {
            ZoneId.of(timezone.trim());
            return timezone.trim();
        } catch (RuntimeException exception) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    private int firstInterval(CollectionReviewSettingEntity setting) {
        List<Integer> intervals = validateIntervals(setting.getIntervalsJson());
        return intervals.get(0);
    }

    private CollectionReviewSettingResponse toResponse(CollectionReviewSettingEntity setting) {
        return new CollectionReviewSettingResponse(
                setting.getId(),
                setting.getCollection().getId(),
                setting.isEnabled(),
                setting.isEmailEnabled(),
                setting.getIntervalsJson(),
                setting.getReminderTime(),
                setting.getTimezone(),
                setting.getLastResetAt(),
                setting.getCreatedAt(),
                setting.getUpdatedAt()
        );
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
