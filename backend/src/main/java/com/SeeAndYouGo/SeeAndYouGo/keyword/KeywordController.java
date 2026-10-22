package com.SeeAndYouGo.SeeAndYouGo.keyword;

import com.SeeAndYouGo.SeeAndYouGo.global.response.ApiResponse;
import com.SeeAndYouGo.SeeAndYouGo.keyword.dto.KeywordAddResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.keyword.dto.KeywordRequestDto;
import com.SeeAndYouGo.SeeAndYouGo.keyword.dto.KeywordResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.user.AuthorizationService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/keyword")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class KeywordController {
    private static final String KEYWORD_ADDED_MESSAGE = "키워드가 등록되었습니다.";
    private static final String KEYWORD_LIMIT_EXCEEDED_MESSAGE = "키워드는 최대 %d개까지 등록할 수 있습니다.";
    private static final String KEYWORD_DELETED_MESSAGE = "키워드가 삭제되었습니다.";

    private final KeywordService keywordService;
    private final AuthorizationService authorizationService;

    @Value("${app.keyword.max-per-user}")
    private int maxKeywordsPerUser;

    @GetMapping("/{user_id}")
    public KeywordResponseDto getKeywordsByUser(@Parameter(hidden = true) @AuthenticationPrincipal String email) {
        authorizationService.getLoginUser(email);
        List<Keyword> keywords = keywordService.getKeywords(email);
        return KeywordResponseDto.toDTO(keywords);
    }

    @PostMapping
    public ApiResponse<KeywordAddResponseDto> addKeyword(@RequestBody KeywordRequestDto keywordRequestDto,
                                                         @Parameter(hidden = true) @AuthenticationPrincipal String email) {
        authorizationService.getLoginUser(email);
        KeywordAddResponseDto response = keywordService.addKeyword(keywordRequestDto.getKeyword(), email);
        boolean exceeded = Boolean.TRUE.equals(response.getIsExceed());
        return ApiResponse.success(response, exceeded ? String.format(KEYWORD_LIMIT_EXCEEDED_MESSAGE, maxKeywordsPerUser) : KEYWORD_ADDED_MESSAGE);
    }

    @DeleteMapping
    public ApiResponse<KeywordResponseDto> deleteKeyword(@RequestBody KeywordRequestDto keywordRequestDto,
                                                         @Parameter(hidden = true) @AuthenticationPrincipal String email) {
        authorizationService.getLoginUser(email);
        return ApiResponse.success(keywordService.deleteKeyword(keywordRequestDto.getKeyword(), email), KEYWORD_DELETED_MESSAGE);
    }
}