package com.SeeAndYouGo.SeeAndYouGo.keyword;

import com.SeeAndYouGo.SeeAndYouGo.keyword.dto.KeywordAddResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.keyword.dto.KeywordResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import com.SeeAndYouGo.SeeAndYouGo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final UserRepository userRepository;
    private final UserKeywordRepository userKeywordRepository;

    public List<Keyword> getKeywords(String email) {
        // 만약 token_id가 넘어오지 않는다면, email은 decoreToEmail()에 의해서 빈 String으로 온다.
        if(email.equals("")) return Collections.emptyList();

        User user = userRepository.findByEmail(email);
        return user.getKeywords();
    }

    @Transactional
    public KeywordAddResponseDto addKeyword(String keywordName, String email) {
        User user = userRepository.findByEmail(email);
        List<UserKeyword> userKeywords = userKeywordRepository.findByUser(user);

        // 유저는 최대 10개의 키워드밖에 등록하지 못한다.
        if (userKeywords.size() >= 10) {
            return KeywordAddResponseDto.toDTO(user.getKeywords(), true);
        }
        if (!keywordRepository.existsByName(keywordName)) {
            keywordRepository.save(new Keyword(keywordName));
        }
        Keyword keyword = keywordRepository.findByName(keywordName);

        user.addKeyword(keyword);
        userRepository.save(user);

        return KeywordAddResponseDto.toDTO(user.getKeywords());
    }

    @Transactional
    public KeywordResponseDto deleteKeyword(String keywordName, String email) throws KeywordNotFoundException {
        Keyword keyword = keywordRepository.findByName(keywordName);
        if (keyword == null) {
            throw new KeywordNotFoundException("키워드 삭제 실패");
        }

        User user = userRepository.findByEmail(email);
        user.deleteKeyword(keyword);

        return KeywordResponseDto.toDTO(user.getKeywords());
    }
}