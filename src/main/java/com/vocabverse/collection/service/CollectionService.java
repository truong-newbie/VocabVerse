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
        UserEntity owner = getCurrentUser();
        CollectionVisibility visibility = resolveCreateVisibility(request.visibility());

        CollectionEntity collection = collectionMapper.toEntity(request);
        collection.setOwner(owner);
        collection.setVisibility(visibility);
        collection.setTotalWords(0);
        collection.setFeatured(false);

        return collectionMapper.toResponse(collectionRepository.save(collection));
    }

    @Transactional(readOnly = true)
    public CollectionPageResponse getMyCollections(Pageable pageable) {
        UUID ownerId = getCurrentUser().getId();
        Page<CollectionResponse> collections = collectionRepository
                .findAllByOwnerIdAndDeletedAtIsNull(ownerId, pageable)
                .map(collectionMapper::toResponse);

        return new CollectionPageResponse(
                collections.getContent(),
                collections.getNumber(),
                collections.getSize(),
                collections.getTotalElements(),
                collections.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public CollectionResponse getCollectionDetail(UUID id) {
        CollectionEntity collection = getOwnedCollection(id);
        return collectionMapper.toResponse(collection);
    }

    @Transactional
    public CollectionResponse updateCollection(UUID id, UpdateCollectionRequest request) {
        CollectionEntity collection = getOwnedCollection(id);
        ensureUserManagedCollection(collection);
        ensureUserAllowedVisibility(request.visibility());

        collectionMapper.updateEntity(request, collection);
        return collectionMapper.toResponse(collectionRepository.save(collection));
    }

    @Transactional
    public void deleteCollection(UUID id) {
        CollectionEntity collection = getOwnedCollection(id);
        ensureUserManagedCollection(collection);

        collection.setDeletedAt(LocalDateTime.now());
        collectionRepository.save(collection);
    }

    private CollectionEntity getOwnedCollection(UUID id) {
        UUID ownerId = getCurrentUser().getId();
        return collectionRepository.findByIdAndOwnerIdAndDeletedAtIsNull(id, ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COLLECTION_NOT_FOUND));
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

    private CollectionVisibility resolveCreateVisibility(CollectionVisibility visibility) {
        if (visibility == null) {
            return CollectionVisibility.PRIVATE;
        }
        ensureUserAllowedVisibility(visibility);
        return visibility;
    }

    private void ensureUserAllowedVisibility(CollectionVisibility visibility) {
        if (visibility == CollectionVisibility.SYSTEM) {
            throw new BusinessException(ErrorCode.COLLECTION_SYSTEM_FORBIDDEN);
        }
    }

    private void ensureUserManagedCollection(CollectionEntity collection) {
        if (collection.getVisibility() == CollectionVisibility.SYSTEM) {
            throw new BusinessException(ErrorCode.COLLECTION_SYSTEM_FORBIDDEN);
        }
    }
}
