package com.SeeAndYouGo.SeeAndYouGo.Keyword;

import com.SeeAndYouGo.SeeAndYouGo.Keyword.dto.KeywordResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import com.SeeAndYouGo.SeeAndYouGo.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final UserRepository userRepository;

    @Autowired
    public KeywordService(KeywordRepository keywordRepository, UserRepository userRepository) {
        this.keywordRepository = keywordRepository;
        this.userRepository = userRepository;
    }

    public List<Keyword> getKeywords(String email) {
        User user = userRepository.findByEmail(email).get(0);
        return user.getKeywords();
    }

    public KeywordResponseDto addKeyword(String keywordName, String email) {
        if (!keywordRepository.existsByName(keywordName)) {
            keywordRepository.save(new Keyword(keywordName));
        }
        Keyword keyword = keywordRepository.findByName(keywordName);
        User user = userRepository.findByEmail(email).get(0);
        user.addKeyword(keyword);
        userRepository.save(user);

        return KeywordResponseDto.toDTO(user.getKeywords());
    }

    @Transactional
    public KeywordResponseDto deleteKeyword(String keywordName, String email) {
        Keyword keyword = keywordRepository.findByName(keywordName);
        if (keyword == null) {
            return null;
        }
        User user = userRepository.findByEmail(email).get(0);
        user.deleteKeyword(keyword);

        return KeywordResponseDto.toDTO(user.getKeywords());
    }
}