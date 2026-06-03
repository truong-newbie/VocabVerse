package com.vocabverse.learning.flashcard.mapper;

import com.vocabverse.learning.flashcard.dto.response.FlashcardCardResponse;
import com.vocabverse.learning.flashcard.dto.response.FlashcardSessionResponse;
import com.vocabverse.learning.flashcard.entity.FlashcardSessionEntity;
import com.vocabverse.learning.flashcard.entity.FlashcardSessionItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FlashcardMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "collectionId", source = "collection.id")
    FlashcardSessionResponse toSessionResponse(FlashcardSessionEntity session);

    @Mapping(target = "vocabularyId", source = "vocabulary.id")
    @Mapping(target = "word", source = "vocabulary.word")
    @Mapping(target = "phonetic", source = "vocabulary.phonetic")
    @Mapping(target = "partOfSpeech", source = "vocabulary.partOfSpeech")
    @Mapping(target = "meaningVi", source = "vocabulary.meaningVi")
    @Mapping(target = "meaningEn", source = "vocabulary.meaningEn")
    FlashcardCardResponse toCardResponse(FlashcardSessionItemEntity item);
}
