package com.SeeAndYouGo.SeeAndYouGo.keyword;

import com.SeeAndYouGo.SeeAndYouGo.keyword.dto.KeywordAddResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.keyword.dto.KeywordRequestDto;
import com.SeeAndYouGo.SeeAndYouGo.keyword.dto.KeywordResponseDto;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/keyword")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class KeywordController {

    private final KeywordService keywordService;

    @GetMapping
    public KeywordResponseDto getKeywordsByUser(@Parameter(hidden = true) @AuthenticationPrincipal String email) {
        List<Keyword> keywords = keywordService.getKeywords(email);
        return KeywordResponseDto.toDTO(keywords);
    }

    @PostMapping
    public KeywordAddResponseDto addKeyword(@RequestBody KeywordRequestDto keywordRequestDto,
                                            @Parameter(hidden = true) @AuthenticationPrincipal String email) {
        log.info("Request to add keyword '{}' for authenticated user.", keywordRequestDto.getKeyword());
        KeywordAddResponseDto response = keywordService.addKeyword(keywordRequestDto.getKeyword(), email);
        log.info("Keyword '{}' addition success. For authenticated user.", keywordRequestDto.getKeyword());
        return response;
    }

    @DeleteMapping
    public KeywordResponseDto deleteKeyword(@RequestBody KeywordRequestDto keywordRequestDto,
                                            @Parameter(hidden = true) @AuthenticationPrincipal String email) {
        log.info("Request to delete keyword '{}' for authenticated user.", keywordRequestDto.getKeyword());
        KeywordResponseDto response = keywordService.deleteKeyword(keywordRequestDto.getKeyword(), email);
        log.info("Keyword '{}' deleted for authenticated user. Now has {} keywords.", keywordRequestDto.getKeyword(), response.getKeywords().size());
        return response;
    }
}