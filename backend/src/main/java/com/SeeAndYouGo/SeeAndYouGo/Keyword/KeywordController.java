package com.SeeAndYouGo.SeeAndYouGo.Keyword;

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

//    @Autowired
//    public KeywordController(KeywordService keywordService, TokenProvider tokenProvider,
//                             UserRepository userRepository) {
//        this.keywordService = keywordService;
//        this.tokenProvider = tokenProvider;
//        this.userRepository = userRepository;
//    }

    @GetMapping("/{user_id}")
    public KeywordResponseDto getKeywordsByUser(@PathVariable String user_id) {
        String email = tokenProvider.decodeToEmail(user_id);
        List<Keyword> keywords = keywordService.getKeywords(email);
        return KeywordResponseDto.toDTO(keywords);
    }

    @PostMapping
    public KeywordResponseDto addKeyword(@RequestBody KeywordRequestDto keywordRequestDto) {
        String email = tokenProvider.decodeToEmail(keywordRequestDto.getUser_id());
        return keywordService.addKeyword(keywordRequestDto.getKeyword(), email);
    }

    @DeleteMapping
    public KeywordResponseDto deleteKeyword(@RequestBody KeywordRequestDto keywordRequestDto) {
        String email = tokenProvider.decodeToEmail(keywordRequestDto.getUser_id());
        return keywordService.deleteKeyword(keywordRequestDto.getKeyword(), email);
    }
}