package com.vocabverse.export.dto;

public record ExportVocabularyRow(
        String term,
        String meaning,
        String pronunciation,
        String partOfSpeech,
        String exampleSentence,
        String vietnameseMeaning,
        String note
) {
}
