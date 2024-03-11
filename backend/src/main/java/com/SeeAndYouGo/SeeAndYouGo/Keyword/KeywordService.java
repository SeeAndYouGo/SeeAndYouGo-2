package com.SeeAndYouGo.SeeAndYouGo.Keyword;

import com.SeeAndYouGo.SeeAndYouGo.Keyword.dto.KeywordAddResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.Keyword.dto.KeywordResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import com.SeeAndYouGo.SeeAndYouGo.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final UserRepository userRepository;
    private final UserKeywordRepository userKeywordRepository;

    @Autowired
    public KeywordService(KeywordRepository keywordRepository, UserRepository userRepository,
                          UserKeywordRepository userKeywordRepository) {
        this.keywordRepository = keywordRepository;
        this.userRepository = userRepository;
        this.userKeywordRepository = userKeywordRepository;
    }

    public List<Keyword> getKeywords(String email) {
        // 만약 token_id가 넘어오지 않는다면, email은 decoreToEmail()에 의해서 빈 String으로 온다.
        if(email.equals("")) return new ArrayList<>();

        User user = userRepository.findByEmail(email).get(0);
        return user.getKeywords();
    }

    public KeywordAddResponseDto addKeyword(String keywordName, String email) {
        User user = userRepository.findByEmail(email).get(0);
        List<UserKeyword> userKeywords = userKeywordRepository.findByUser(user);
        if (userKeywords.size() >= 10) {
            return KeywordAddResponseDto.toDTO(user.getKeywords(), true );
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
        User user = userRepository.findByEmail(email).get(0);
        user.deleteKeyword(keyword);

        return KeywordResponseDto.toDTO(user.getKeywords());
    }
}