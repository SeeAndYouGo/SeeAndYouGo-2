package com.SeeAndYouGo.SeeAndYouGo.keyword;

import com.SeeAndYouGo.SeeAndYouGo.aop.ValidateToken;
import com.SeeAndYouGo.SeeAndYouGo.keyword.dto.KeywordAddResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.keyword.dto.KeywordRequestDto;
import com.SeeAndYouGo.SeeAndYouGo.keyword.dto.KeywordResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/keyword")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class KeywordController {

    private final KeywordService keywordService;

    @GetMapping("/{user_id}")
    @ValidateToken
    public KeywordResponseDto getKeywordsByUser(@PathVariable("user_id") String tokenId,
                                                @AuthenticationPrincipal String email) {
        List<Keyword> keywords = keywordService.getKeywords(email);
        return KeywordResponseDto.toDTO(keywords);
    }

    @PostMapping
    public KeywordAddResponseDto addKeyword(@RequestBody KeywordRequestDto keywordRequestDto,
                                            @AuthenticationPrincipal String email) {
        return keywordService.addKeyword(keywordRequestDto.getKeyword(), email);
    }

    @DeleteMapping
    public KeywordResponseDto deleteKeyword(@RequestBody KeywordRequestDto keywordRequestDto,
                                            @AuthenticationPrincipal String email) {
        return keywordService.deleteKeyword(keywordRequestDto.getKeyword(), email);
    }
}