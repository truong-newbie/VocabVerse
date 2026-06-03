package com.vocabverse.collection.service;

import com.vocabverse.collection.dto.request.CreateCollectionRequest;
import com.vocabverse.collection.dto.request.UpdateCollectionRequest;
import com.vocabverse.collection.dto.response.CollectionPageResponse;
import com.vocabverse.collection.dto.response.CollectionResponse;
import com.vocabverse.collection.entity.CollectionEntity;
import com.vocabverse.collection.enums.CollectionVisibility;
import com.vocabverse.collection.mapper.CollectionMapper;
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
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final CollectionMapper collectionMapper;

    @Transactional
    public CollectionResponse createCollection(CreateCollectionRequest request) {
        rejectSystemVisibility(request.visibility());

        UserEntity owner = getCurrentUser();
        CollectionEntity collection = collectionMapper.toEntity(request);
        collection.setOwner(owner);
        collection.setTotalWords(0);
        collection.setFeatured(false);

        return collectionMapper.toResponse(collectionRepository.save(collection));
    }

    @Transactional(readOnly = true)
    public CollectionPageResponse getMyCollections(Pageable pageable) {
        UUID ownerId = getCurrentUser().getId();
        Page<CollectionResponse> page = collectionRepository
                .findAllByOwnerIdAndDeletedAtIsNull(ownerId, pageable)
                .map(collectionMapper::toResponse);

        return toPageResponse(page);
    }

    @Transactional(readOnly = true)
    public CollectionResponse getCollectionDetail(UUID collectionId) {
        return collectionMapper.toResponse(getOwnedCollection(collectionId));
    }

    @Transactional
    public CollectionResponse updateCollection(UUID collectionId, UpdateCollectionRequest request) {
        rejectSystemVisibility(request.visibility());

        CollectionEntity collection = getOwnedCollection(collectionId);
        if (collection.getVisibility() == CollectionVisibility.SYSTEM) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        collectionMapper.updateEntity(request, collection);
        return collectionMapper.toResponse(collectionRepository.save(collection));
    }

    @Transactional
    public void deleteCollection(UUID collectionId) {
        CollectionEntity collection = getOwnedCollection(collectionId);
        if (collection.getVisibility() == CollectionVisibility.SYSTEM) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        collection.setDeletedAt(LocalDateTime.now());
        collectionRepository.save(collection);
    }

    private CollectionEntity getOwnedCollection(UUID collectionId) {
        UUID ownerId = getCurrentUser().getId();
        return collectionRepository.findByIdAndOwnerIdAndDeletedAtIsNull(collectionId, ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COLLECTION_NOT_FOUND));
    }

    private void rejectSystemVisibility(CollectionVisibility visibility) {
        if (visibility == CollectionVisibility.SYSTEM) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
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

    private CollectionPageResponse toPageResponse(Page<CollectionResponse> page) {
        return new CollectionPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
