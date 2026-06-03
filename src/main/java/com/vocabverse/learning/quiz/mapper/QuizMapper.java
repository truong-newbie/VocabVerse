package com.vocabverse.learning.quiz.mapper;

import com.vocabverse.learning.quiz.dto.response.QuizQuestionResponse;
import com.vocabverse.learning.quiz.dto.response.QuizSessionResponse;
import com.vocabverse.learning.quiz.entity.QuizQuestionEntity;
import com.vocabverse.learning.quiz.entity.QuizSessionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QuizMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "collectionId", source = "collection.id")
    QuizSessionResponse toSessionResponse(QuizSessionEntity session);

    @Mapping(target = "vocabularyId", source = "vocabulary.id")
    QuizQuestionResponse toQuestionResponse(QuizQuestionEntity question);
}
