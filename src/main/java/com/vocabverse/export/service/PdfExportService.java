package com.vocabverse.export.service;

import com.vocabverse.collection.entity.CollectionEntity;
import com.vocabverse.collection.enums.CollectionVisibility;
import com.vocabverse.collection.repository.CollectionRepository;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.export.dto.ExportVocabularyRow;
import com.vocabverse.export.dto.PdfExportResult;
import com.vocabverse.export.template.PdfTemplateBuilder;
import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.user.repository.UserRepository;
import com.vocabverse.vocabulary.entity.CollectionVocabularyEntity;
import com.vocabverse.vocabulary.entity.VocabularyEntity;
import com.vocabverse.vocabulary.entity.VocabularyExample;
import com.vocabverse.vocabulary.repository.CollectionVocabularyRepository;
import com.vocabverse.vocabulary.repository.VocabularyRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PdfExportService {

    private static final List<CollectionVisibility> EXPORTABLE_COLLECTION_VISIBILITIES = List.of(
            CollectionVisibility.PRIVATE,
            CollectionVisibility.PUBLIC
    );

    private final CollectionRepository collectionRepository;
    private final CollectionVocabularyRepository collectionVocabularyRepository;
    private final VocabularyRepository vocabularyRepository;
    private final UserRepository userRepository;
    private final PdfTemplateBuilder pdfTemplateBuilder;

    @Transactional(readOnly = true)
    public PdfExportResult exportCollection(UUID collectionId) {
        UserEntity user = getCurrentUser();
        CollectionEntity collection = collectionRepository
                .findByIdAndOwnerIdAndVisibilityInAndDeletedAtIsNull(
                        collectionId,
                        user.getId(),
                        EXPORTABLE_COLLECTION_VISIBILITIES
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.COLLECTION_NOT_FOUND));

        List<ExportVocabularyRow> rows = collectionVocabularyRepository
                .findAllByCollectionIdAndVocabularyDeletedAtIsNull(collection.getId())
                .stream()
                .map(CollectionVocabularyEntity::getVocabulary)
                .map(this::toExportRow)
                .toList();

        return buildResult(collection.getTitle(), rows, safeFilename(collection.getTitle()) + ".pdf");
    }

    @Transactional(readOnly = true)
    public PdfExportResult exportAllVocabularies() {
        UserEntity user = getCurrentUser();
        List<ExportVocabularyRow> rows = vocabularyRepository.findAllByOwnerIdAndDeletedAtIsNull(user.getId())
                .stream()
                .map(this::toExportRow)
                .toList();

        return buildResult("All Vocabularies", rows, "vocabverse-vocabularies.pdf");
    }

    private PdfExportResult buildResult(String title, List<ExportVocabularyRow> rows, String filename) {
        try {
            return new PdfExportResult(filename, pdfTemplateBuilder.buildVocabularyPdf(title, rows));
        } catch (RuntimeException exception) {
            throw new BusinessException(ErrorCode.PDF_GENERATION_FAILED, ErrorCode.PDF_GENERATION_FAILED.getMessage(), exception);
        }
    }

    private ExportVocabularyRow toExportRow(VocabularyEntity vocabulary) {
        VocabularyExample example = firstExample(vocabulary);
        return new ExportVocabularyRow(
                vocabulary.getWord(),
                firstNonBlank(vocabulary.getMeaningEn(), vocabulary.getMeaningVi()),
                vocabulary.getPhonetic(),
                vocabulary.getPartOfSpeech(),
                example == null ? null : example.getEn(),
                vocabulary.getMeaningVi(),
                null
        );
    }

    private VocabularyExample firstExample(VocabularyEntity vocabulary) {
        if (vocabulary.getExamples() == null || vocabulary.getExamples().isEmpty()) {
            return null;
        }
        return vocabulary.getExamples().get(0);
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback;
    }

    private String safeFilename(String title) {
        String filename = title == null ? "vocabverse-export" : title.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (filename.isBlank()) {
            return "vocabverse-export";
        }
        return filename;
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
