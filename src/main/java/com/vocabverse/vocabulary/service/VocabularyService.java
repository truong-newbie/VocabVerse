package com.vocabverse.vocabulary.service;

import com.vocabverse.collection.entity.CollectionEntity;
import com.vocabverse.collection.repository.CollectionRepository;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.user.repository.UserRepository;
import com.vocabverse.vocabulary.dto.request.CreateVocabularyRequest;
import com.vocabverse.vocabulary.dto.request.UpdateVocabularyRequest;
import com.vocabverse.vocabulary.dto.response.VocabularyPageResponse;
import com.vocabverse.vocabulary.dto.response.VocabularyResponse;
import com.vocabverse.vocabulary.entity.CollectionVocabularyEntity;
import com.vocabverse.vocabulary.entity.VocabularyEntity;
import com.vocabverse.vocabulary.entity.VocabularySource;
import com.vocabverse.vocabulary.mapper.VocabularyMapper;
import com.vocabverse.vocabulary.repository.CollectionVocabularyRepository;
import com.vocabverse.vocabulary.repository.VocabularyRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final CollectionRepository collectionRepository;
    private final CollectionVocabularyRepository collectionVocabularyRepository;
    private final UserRepository userRepository;
    private final VocabularyMapper vocabularyMapper;

    @Transactional
    public VocabularyResponse createVocabulary(CreateVocabularyRequest request) {
        UserEntity owner = getCurrentUser();
        VocabularyEntity vocabulary = vocabularyMapper.toEntity(request);
        vocabulary.setOwner(owner);
        vocabulary.setNormalizedWord(normalizeWord(request.word()));
        vocabulary.setSource(VocabularySource.MANUAL);

        VocabularyEntity savedVocabulary = vocabularyRepository.save(vocabulary);
        attachToCollections(savedVocabulary, request.collectionIds(), owner);

        return vocabularyMapper.toResponse(savedVocabulary);
    }

    @Transactional(readOnly = true)
    public VocabularyPageResponse getMyVocabularies(Pageable pageable) {
        UUID ownerId = getCurrentUser().getId();
        Page<VocabularyResponse> vocabularies = vocabularyRepository
                .findAllByOwnerIdAndDeletedAtIsNull(ownerId, pageable)
                .map(vocabularyMapper::toResponse);

        return toPageResponse(vocabularies);
    }

    @Transactional(readOnly = true)
    public VocabularyResponse getVocabularyDetail(UUID id) {
        return vocabularyMapper.toResponse(getOwnedVocabulary(id));
    }

    @Transactional
    public VocabularyResponse updateVocabulary(UUID id, UpdateVocabularyRequest request) {
        VocabularyEntity vocabulary = getOwnedVocabulary(id);

        vocabularyMapper.updateEntity(request, vocabulary);
        vocabulary.setNormalizedWord(normalizeWord(request.word()));

        return vocabularyMapper.toResponse(vocabularyRepository.save(vocabulary));
    }

    @Transactional
    public void deleteVocabulary(UUID id) {
        VocabularyEntity vocabulary = getOwnedVocabulary(id);
        List<CollectionVocabularyEntity> relations = collectionVocabularyRepository.findAllByVocabularyId(id);
        for (CollectionVocabularyEntity relation : relations) {
            decrementTotalWords(relation.getCollection());
        }
        collectionVocabularyRepository.deleteAll(relations);

        vocabulary.setDeletedAt(LocalDateTime.now());
        vocabularyRepository.save(vocabulary);
    }

    @Transactional
    public VocabularyResponse addVocabularyToCollection(UUID collectionId, UUID vocabularyId) {
        UserEntity currentUser = getCurrentUser();
        CollectionEntity collection = getOwnedCollection(collectionId, currentUser.getId());
        VocabularyEntity vocabulary = getOwnedVocabulary(vocabularyId, currentUser.getId());

        if (collectionVocabularyRepository
                .existsByCollectionIdAndVocabularyIdAndCollectionDeletedAtIsNullAndVocabularyDeletedAtIsNull(
                        collectionId,
                        vocabularyId
                )) {
            throw new BusinessException(ErrorCode.DUPLICATE_WORD);
        }

        CollectionVocabularyEntity relation = CollectionVocabularyEntity.builder()
                .collection(collection)
                .vocabulary(vocabulary)
                .addedBy(currentUser)
                .position(0)
                .build();
        collectionVocabularyRepository.save(relation);
        incrementTotalWords(collection);

        return vocabularyMapper.toResponse(vocabulary);
    }

    @Transactional
    public void removeVocabularyFromCollection(UUID collectionId, UUID vocabularyId) {
        UserEntity currentUser = getCurrentUser();
        CollectionEntity collection = getOwnedCollection(collectionId, currentUser.getId());

        CollectionVocabularyEntity relation = collectionVocabularyRepository
                .findByCollectionIdAndVocabularyIdAndCollectionDeletedAtIsNullAndVocabularyDeletedAtIsNull(
                        collectionId,
                        vocabularyId
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.VOCABULARY_NOT_FOUND));

        collectionVocabularyRepository.delete(relation);
        decrementTotalWords(collection);
    }

    @Transactional(readOnly = true)
    public VocabularyPageResponse getCollectionVocabularies(UUID collectionId, Pageable pageable) {
        UUID ownerId = getCurrentUser().getId();
        getOwnedCollection(collectionId, ownerId);

        Page<VocabularyResponse> vocabularies = collectionVocabularyRepository
                .findAllByCollectionIdAndCollectionDeletedAtIsNullAndVocabularyDeletedAtIsNull(collectionId, pageable)
                .map(CollectionVocabularyEntity::getVocabulary)
                .map(vocabularyMapper::toResponse);

        return toPageResponse(vocabularies);
    }

    private void attachToCollections(VocabularyEntity vocabulary, Set<UUID> collectionIds, UserEntity owner) {
        if (collectionIds == null || collectionIds.isEmpty()) {
            return;
        }

        List<CollectionEntity> collections = collectionRepository
                .findAllByIdInAndOwnerIdAndDeletedAtIsNull(collectionIds, owner.getId());
        if (collections.size() != collectionIds.size()) {
            throw new BusinessException(ErrorCode.COLLECTION_NOT_FOUND);
        }

        for (CollectionEntity collection : collections) {
            CollectionVocabularyEntity relation = CollectionVocabularyEntity.builder()
                    .collection(collection)
                    .vocabulary(vocabulary)
                    .addedBy(owner)
                    .position(0)
                    .build();
            collectionVocabularyRepository.save(relation);
            incrementTotalWords(collection);
        }
    }

    private CollectionEntity getOwnedCollection(UUID collectionId, UUID ownerId) {
        return collectionRepository.findByIdAndOwnerIdAndDeletedAtIsNull(collectionId, ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COLLECTION_NOT_FOUND));
    }

    private VocabularyEntity getOwnedVocabulary(UUID vocabularyId) {
        return getOwnedVocabulary(vocabularyId, getCurrentUser().getId());
    }

    private VocabularyEntity getOwnedVocabulary(UUID vocabularyId, UUID ownerId) {
        return vocabularyRepository.findByIdAndOwnerIdAndDeletedAtIsNull(vocabularyId, ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VOCABULARY_NOT_FOUND));
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

    private String normalizeWord(String word) {
        return word.trim().toLowerCase(Locale.ROOT);
    }

    private void incrementTotalWords(CollectionEntity collection) {
        collection.setTotalWords(collection.getTotalWords() + 1);
        collectionRepository.save(collection);
    }

    private void decrementTotalWords(CollectionEntity collection) {
        collection.setTotalWords(Math.max(0, collection.getTotalWords() - 1));
        collectionRepository.save(collection);
    }

    private VocabularyPageResponse toPageResponse(Page<VocabularyResponse> page) {
        return new VocabularyPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
