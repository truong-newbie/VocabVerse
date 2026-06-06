package com.vocabverse.dictionary.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabverse.dictionary.dto.response.DictionaryWordResponse;
import org.junit.jupiter.api.Test;

class DictionaryMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DictionaryMapper dictionaryMapper = new DictionaryMapper();

    @Test
    void mapsDictionaryApiDevResponseToUnifiedResponse() throws Exception {
        JsonNode root = objectMapper.readTree("""
                [
                  {
                    "word": "abandon",
                    "phonetics": [
                      {
                        "text": "/uh-BAN-duhn/",
                        "audio": "https://example.com/audio.mp3"
                      }
                    ],
                    "meanings": [
                      {
                        "partOfSpeech": "verb",
                        "synonyms": ["leave"],
                        "antonyms": ["keep"],
                        "definitions": [
                          {
                            "definition": "To leave permanently.",
                            "example": "He abandoned the project.",
                            "synonyms": ["desert"],
                            "antonyms": []
                          }
                        ]
                      }
                    ]
                  }
                ]
                """);

        DictionaryWordResponse response = dictionaryMapper.toResponse(root);

        assertThat(response.word()).isEqualTo("abandon");
        assertThat(response.phonetic()).isEqualTo("/uh-BAN-duhn/");
        assertThat(response.audioUrl()).isEqualTo("https://example.com/audio.mp3");
        assertThat(response.meanings()).hasSize(1);
        assertThat(response.meanings().get(0).partOfSpeech()).isEqualTo("verb");
        assertThat(response.meanings().get(0).definitions().get(0).definition())
                .isEqualTo("To leave permanently.");
        assertThat(response.meanings().get(0).definitions().get(0).synonyms())
                .containsExactly("leave", "desert");
        assertThat(response.meanings().get(0).definitions().get(0).antonyms())
                .containsExactly("keep");
    }
}
