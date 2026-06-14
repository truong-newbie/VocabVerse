package com.vocabverse.dictionary.controller;

import com.vocabverse.common.response.ApiResponse;
import com.vocabverse.dictionary.dto.response.DictionaryWordResponse;
import com.vocabverse.dictionary.service.DictionaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dictionary")
public class DictionaryController {

    private final DictionaryService dictionaryService;

    @GetMapping("/search")
    public ApiResponse<DictionaryWordResponse> search(@RequestParam(required = false) String word) {
        return ApiResponse.success(dictionaryService.search(word));
    }
}
