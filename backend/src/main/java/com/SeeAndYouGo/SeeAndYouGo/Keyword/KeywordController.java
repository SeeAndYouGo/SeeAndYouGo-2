package com.SeeAndYouGo.SeeAndYouGo.Keyword;

import com.SeeAndYouGo.SeeAndYouGo.AOP.ValidateToken;
import com.SeeAndYouGo.SeeAndYouGo.Keyword.dto.KeywordAddResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.Keyword.dto.KeywordRequestDto;
import com.SeeAndYouGo.SeeAndYouGo.Keyword.dto.KeywordResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.OAuth.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/keyword")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class KeywordController {

    private final KeywordService keywordService;
    private final TokenProvider tokenProvider;

    @GetMapping("/{user_id}")
    @ValidateToken
    public KeywordResponseDto getKeywordsByUser(@PathVariable("user_id") String tokenId) {
        String email = tokenProvider.decodeToEmail(tokenId);
        List<Keyword> keywords = keywordService.getKeywords(email);
        return KeywordResponseDto.toDTO(keywords);
    }

    @PostMapping
    public KeywordAddResponseDto addKeyword(@RequestBody KeywordRequestDto keywordRequestDto) {
        String email = tokenProvider.decodeToEmail(keywordRequestDto.getUser_id());
        return keywordService.addKeyword(keywordRequestDto.getKeyword(), email);
    }

    @DeleteMapping
    public KeywordResponseDto deleteKeyword(@RequestBody KeywordRequestDto keywordRequestDto) {
        String email = tokenProvider.decodeToEmail(keywordRequestDto.getUser_id());
        return keywordService.deleteKeyword(keywordRequestDto.getKeyword(), email);
    }
}