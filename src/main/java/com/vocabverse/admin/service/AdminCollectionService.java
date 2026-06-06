package com.vocabverse.admin.service;

import com.vocabverse.admin.dto.request.AdminModerateCollectionRequest;
import com.vocabverse.admin.dto.request.AdminHidePublicCollectionRequest;
import com.vocabverse.admin.dto.request.AdminUpdateCollectionVisibilityRequest;
import com.vocabverse.admin.dto.response.AdminCollectionModerationResponse;
import com.vocabverse.admin.dto.response.AdminCollectionResponse;
import com.vocabverse.admin.dto.response.AdminPageResponse;
import com.vocabverse.admin.dto.response.AdminPublicCollectionHideResponse;
import com.vocabverse.admin.dto.response.AdminPublicVocabularyResponse;
import com.vocabverse.admin.entity.PublicCollectionModerationEntity;
import com.vocabverse.admin.repository.PublicCollectionModerationRepository;
import com.vocabverse.collection.entity.CollectionEntity;
import com.vocabverse.collection.enums.CollectionVisibility;
import com.vocabverse.collection.repository.CollectionRepository;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.user.repository.UserRepository;
import com.vocabverse.vocabulary.entity.CollectionVocabularyEntity;
import com.vocabverse.vocabulary.entity.VocabularyEntity;
import com.vocabverse.vocabulary.repository.CollectionVocabularyRepository;
import java.time.LocalDateTime;
import java.util.Locale;
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
public class AdminCollectionService {

    private final CollectionRepository collectionRepository;
    private final PublicCollectionModerationRepository moderationRepository;
    private final UserRepository userRepository;
    private final CollectionVocabularyRepository collectionVocabularyRepository;

    @Transactional(readOnly = true)
    public AdminPageResponse<AdminCollectionResponse> listCollections(
            CollectionVisibility visibility,
            Pageable pageable
    ) {
        Page<CollectionEntity> page = visibility == null
                ? collectionRepository.findAllByDeletedAtIsNull(pageable)
                : collectionRepository.findAllByVisibilityAndDeletedAtIsNull(visibility, pageable);

        return toPageResponse(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public AdminCollectionResponse getCollection(UUID collectionId) {
        return toResponse(findCollection(collectionId));
    }

    @Transactional
    public AdminCollectionResponse updateVisibility(
            UUID collectionId,
            AdminUpdateCollectionVisibilityRequest request
    ) {
        CollectionEntity collection = findCollection(collectionId);
        collection.setVisibility(request.visibility());
        return toResponse(collectionRepository.save(collection));
    }

    @Transactional
    public AdminCollectionModerationResponse moderateCollection(
            UUID collectionId,
            AdminModerateCollectionRequest request
    ) {
        CollectionEntity collection = findCollection(collectionId);
        String action = request.action().trim().toUpperCase(Locale.ROOT);
        if ("APPROVE".equals(action)) {
            collection.setVisibility(CollectionVisibility.PUBLIC);
            collectionRepository.save(collection);
            return new AdminCollectionModerationResponse(collection.getId(), "APPROVED", collection.getVisibility());
        }
        if ("HIDE".equals(action) || "REJECT".equals(action)) {
            collection.setVisibility(CollectionVisibility.PRIVATE);
            collectionRepository.save(collection);
            moderationRepository.save(PublicCollectionModerationEntity.builder()
                    .collection(collection)
                    .moderatedBy(getCurrentAdmin())
                    .reason(request.reason() == null || request.reason().isBlank()
                            ? action
                            : request.reason().trim())
                    .build());
            return new AdminCollectionModerationResponse(collection.getId(), "HIDDEN", collection.getVisibility());
        }
        throw new BusinessException(ErrorCode.INVALID_INPUT, "Unsupported moderation action");
    }

    @Transactional
    public void deleteCollection(UUID collectionId) {
        CollectionEntity collection = findCollection(collectionId);
        if (collection.getVisibility() == CollectionVisibility.SYSTEM) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "SYSTEM collection cannot be deleted");
        }

        collection.setDeletedAt(LocalDateTime.now());
        collectionRepository.save(collection);
    }

    @Transactional(readOnly = true)
    public AdminPageResponse<AdminCollectionResponse> listPublicCollections(Pageable pageable) {
        return listCollections(CollectionVisibility.PUBLIC, pageable);
    }

    @Transactional
    public AdminCollectionResponse hidePublicCollectionAsCollection(
            UUID collectionId,
            AdminHidePublicCollectionRequest request
    ) {
        hidePublicCollection(collectionId, request);
        return toResponse(findCollection(collectionId));
    }

    @Transactional
    public AdminPublicCollectionHideResponse hidePublicCollection(
            UUID collectionId,
            AdminHidePublicCollectionRequest request
    ) {
        CollectionEntity collection = collectionRepository
                .findByIdAndVisibilityAndDeletedAtIsNull(collectionId, CollectionVisibility.PUBLIC)
                .orElseThrow(() -> new BusinessException(ErrorCode.PUBLIC_COLLECTION_NOT_FOUND));

        collection.setVisibility(CollectionVisibility.PRIVATE);
        moderationRepository.save(PublicCollectionModerationEntity.builder()
                .collection(collection)
                .moderatedBy(getCurrentAdmin())
                .reason(request.reason().trim())
                .build());

        collectionRepository.save(collection);
        return new AdminPublicCollectionHideResponse(collection.getId(), "HIDDEN", request.reason().trim());
    }

    @Transactional(readOnly = true)
    public AdminPageResponse<AdminPublicVocabularyResponse> getPublicCollectionVocabularies(
            UUID collectionId,
            Pageable pageable
    ) {
        collectionRepository.findByIdAndVisibilityAndDeletedAtIsNull(collectionId, CollectionVisibility.PUBLIC)
                .orElseThrow(() -> new BusinessException(ErrorCode.PUBLIC_COLLECTION_NOT_FOUND));

        Page<AdminPublicVocabularyResponse> page = collectionVocabularyRepository
                .findAllByCollectionIdAndCollectionVisibilityAndCollectionDeletedAtIsNullAndVocabularyDeletedAtIsNull(
                        collectionId,
                        CollectionVisibility.PUBLIC,
                        pageable
                )
                .map(CollectionVocabularyEntity::getVocabulary)
                .map(this::toPublicVocabularyResponse);
        return new AdminPageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private CollectionEntity findCollection(UUID collectionId) {
        return collectionRepository.findByIdAndDeletedAtIsNull(collectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COLLECTION_NOT_FOUND));
    }

    private UserEntity getCurrentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private AdminCollectionResponse toResponse(CollectionEntity collection) {
        UserEntity owner = collection.getOwner();
        return new AdminCollectionResponse(
                collection.getId(),
                owner == null ? null : owner.getId(),
                owner == null ? null : owner.getEmail(),
                owner == null ? null : owner.getFullName(),
                collection.getTitle(),
                collection.getDescription(),
                collection.getVisibility(),
                collection.getVisibility().name(),
                collection.getThumbnailUrl(),
                collection.getTotalWords(),
                collection.getTotalWords(),
                collection.isFeatured(),
                collection.getCreatedAt(),
                collection.getUpdatedAt()
        );
    }

    private AdminPageResponse<AdminCollectionResponse> toPageResponse(Page<AdminCollectionResponse> page) {
        return new AdminPageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private AdminPublicVocabularyResponse toPublicVocabularyResponse(VocabularyEntity vocabulary) {
        return new AdminPublicVocabularyResponse(
                vocabulary.getId(),
                vocabulary.getWord(),
                vocabulary.getMeaningEn()
        );
    }
}
