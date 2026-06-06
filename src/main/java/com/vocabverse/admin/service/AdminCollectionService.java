package com.vocabverse.admin.service;

import com.vocabverse.admin.dto.request.AdminHidePublicCollectionRequest;
import com.vocabverse.admin.dto.request.AdminUpdateCollectionVisibilityRequest;
import com.vocabverse.admin.dto.response.AdminCollectionResponse;
import com.vocabverse.admin.dto.response.AdminPageResponse;
import com.vocabverse.admin.entity.PublicCollectionModerationEntity;
import com.vocabverse.admin.repository.PublicCollectionModerationRepository;
import com.vocabverse.collection.entity.CollectionEntity;
import com.vocabverse.collection.enums.CollectionVisibility;
import com.vocabverse.collection.repository.CollectionRepository;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.user.repository.UserRepository;
import java.time.LocalDateTime;
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
    public AdminCollectionResponse hidePublicCollection(
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

        return toResponse(collectionRepository.save(collection));
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
                collection.getTitle(),
                collection.getDescription(),
                collection.getVisibility(),
                collection.getThumbnailUrl(),
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
}
