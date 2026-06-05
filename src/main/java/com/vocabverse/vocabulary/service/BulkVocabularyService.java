package com.vocabverse.vocabulary.service;

import com.vocabverse.collection.entity.CollectionEntity;
import com.vocabverse.collection.repository.CollectionRepository;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.user.repository.UserRepository;
import com.vocabverse.vocabulary.dto.request.BulkCreateVocabularyItemRequest;
import com.vocabverse.vocabulary.dto.request.BulkCreateVocabularyRequest;
import com.vocabverse.vocabulary.dto.response.BulkCreateVocabularyFailedItemResponse;
import com.vocabverse.vocabulary.dto.response.BulkCreateVocabularyResponse;
import com.vocabverse.vocabulary.entity.CollectionVocabularyEntity;
import com.vocabverse.vocabulary.entity.VocabularyEntity;
import com.vocabverse.vocabulary.entity.VocabularyExample;
import com.vocabverse.vocabulary.entity.VocabularySource;
import com.vocabverse.vocabulary.repository.CollectionVocabularyRepository;
import com.vocabverse.vocabulary.repository.VocabularyRepository;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BulkVocabularyService {

    private static final int TERM_MAX_LENGTH = 150;
    private static final int MEANING_MAX_LENGTH = 2000;
    private static final int VIETNAMESE_MEANING_MAX_LENGTH = 2000;
    private static final int PRONUNCIATION_MAX_LENGTH = 100;
    private static final int PART_OF_SPEECH_MAX_LENGTH = 50;
    private static final int EXAMPLE_MAX_LENGTH = 2000;
    private static final int NOTE_MAX_LENGTH = 2000;

    private final VocabularyRepository vocabularyRepository;
    private final CollectionRepository collectionRepository;
    private final CollectionVocabularyRepository collectionVocabularyRepository;
    private final UserRepository userRepository;

    @Transactional
    public BulkCreateVocabularyResponse bulkCreate(UUID collectionId, BulkCreateVocabularyRequest request) {
        UserEntity user = getCurrentUser();
        CollectionEntity collection = collectionRepository.findByIdAndOwnerIdAndDeletedAtIsNull(collectionId, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COLLECTION_NOT_FOUND));

        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        List<BulkCreateVocabularyFailedItemResponse> failedItems = new ArrayList<>();
        Set<String> importedTerms = new LinkedHashSet<>();
        int successCount = 0;

        for (int index = 0; index < request.items().size(); index++) {
            int row = index + 1;
            BulkCreateVocabularyItemRequest item = request.items().get(index);
            String validationError = validateItem(item);
            String term = item == null ? null : item.term();
            if (validationError != null) {
                failedItems.add(new BulkCreateVocabularyFailedItemResponse(row, term, validationError));
                continue;
            }

            String normalizedTerm = normalize(term);
            if (!importedTerms.add(normalizedTerm)
                    || collectionVocabularyRepository
                    .existsByCollectionIdAndVocabularyNormalizedWordAndCollectionDeletedAtIsNullAndVocabularyDeletedAtIsNull(
                            collectionId,
                            normalizedTerm
                    )) {
                failedItems.add(new BulkCreateVocabularyFailedItemResponse(row, term, "duplicate term in collection"));
                continue;
            }

            VocabularyEntity vocabulary = vocabularyRepository.save(toVocabulary(item, user, normalizedTerm));
            collectionVocabularyRepository.save(CollectionVocabularyEntity.builder()
                    .collection(collection)
                    .vocabulary(vocabulary)
                    .addedBy(user)
                    .position(0)
                    .build());
            successCount++;
        }

        if (successCount > 0) {
            collection.setTotalWords(collection.getTotalWords() + successCount);
            collectionRepository.save(collection);
        }

        return new BulkCreateVocabularyResponse(successCount, failedItems.size(), failedItems);
    }

    private VocabularyEntity toVocabulary(
            BulkCreateVocabularyItemRequest item,
            UserEntity user,
            String normalizedTerm
    ) {
        String exampleSentence = trimToNull(item.exampleSentence());
        List<VocabularyExample> examples = exampleSentence == null
                ? List.of()
                : List.of(new VocabularyExample(exampleSentence, null));

        return VocabularyEntity.builder()
                .owner(user)
                .word(item.term().trim())
                .normalizedWord(normalizedTerm)
                .phonetic(trimToNull(item.pronunciation()))
                .partOfSpeech(trimToNull(item.partOfSpeech()))
                .meaningEn(item.meaning().trim())
                .meaningVi(trimToNull(item.vietnameseMeaning()))
                .examples(examples)
                .synonyms(List.of())
                .antonyms(List.of())
                .source(VocabularySource.MANUAL)
                .build();
    }

    private String validateItem(BulkCreateVocabularyItemRequest item) {
        if (item == null) {
            return "item is required";
        }
        if (isBlank(item.term())) {
            return "term is required";
        }
        if (item.term().trim().length() > TERM_MAX_LENGTH) {
            return "term must be at most " + TERM_MAX_LENGTH + " characters";
        }
        if (isBlank(item.meaning())) {
            return "meaning is required";
        }
        if (item.meaning().trim().length() > MEANING_MAX_LENGTH) {
            return "meaning must be at most " + MEANING_MAX_LENGTH + " characters";
        }
        if (lengthExceeds(item.vietnameseMeaning(), VIETNAMESE_MEANING_MAX_LENGTH)) {
            return "vietnameseMeaning must be at most " + VIETNAMESE_MEANING_MAX_LENGTH + " characters";
        }
        if (lengthExceeds(item.pronunciation(), PRONUNCIATION_MAX_LENGTH)) {
            return "pronunciation must be at most " + PRONUNCIATION_MAX_LENGTH + " characters";
        }
        if (lengthExceeds(item.partOfSpeech(), PART_OF_SPEECH_MAX_LENGTH)) {
            return "partOfSpeech must be at most " + PART_OF_SPEECH_MAX_LENGTH + " characters";
        }
        if (lengthExceeds(item.exampleSentence(), EXAMPLE_MAX_LENGTH)) {
            return "exampleSentence must be at most " + EXAMPLE_MAX_LENGTH + " characters";
        }
        if (lengthExceeds(item.note(), NOTE_MAX_LENGTH)) {
            return "note must be at most " + NOTE_MAX_LENGTH + " characters";
        }
        return null;
    }

    private boolean lengthExceeds(String value, int maxLength) {
        return value != null && value.trim().length() > maxLength;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
    }

    private String normalize(String word) {
        return word.trim().toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }
        return value.trim();
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
