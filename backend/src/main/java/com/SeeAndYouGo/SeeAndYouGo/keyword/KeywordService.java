package com.SeeAndYouGo.SeeAndYouGo.keyword;

import com.SeeAndYouGo.SeeAndYouGo.keyword.dto.KeywordAddResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.keyword.dto.KeywordResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import com.SeeAndYouGo.SeeAndYouGo.user.UserRepository;
import com.SeeAndYouGo.SeeAndYouGo.userKeyword.UserKeyword;
import com.SeeAndYouGo.SeeAndYouGo.userKeyword.UserKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final UserRepository userRepository;
    private final UserKeywordRepository userKeywordRepository;

    public List<Keyword> getKeywords(String email) {
        // 만약 token_id가 넘어오지 않는다면, email은 decoreToEmail()에 의해서 빈 String으로 온다.
        if(email == null || email.isEmpty()) return Collections.emptyList();

        User user = userRepository.findByEmail(email);
        if (user == null) {
            return Collections.emptyList();
        }
        return user.getKeywords();
    }

    @Transactional
    public KeywordAddResponseDto addKeyword(String keywordName, String email) {
        log.info("Adding keyword '{}' for a user.", keywordName);
        User user = userRepository.findByEmail(email);
        if (user == null) {
            log.error("Cannot add keyword, user not found.");
            throw new javax.persistence.EntityNotFoundException("Cannot add keyword, user not found.");
        }

        List<UserKeyword> userKeywords = userKeywordRepository.findByUser(user);

        // 유저는 최대 10개의 키워드밖에 등록하지 못한다.
        if (userKeywords.size() >= 10) {
            log.warn("User ID: {} has reached the maximum keyword limit of 10.", user.getId());
            return KeywordAddResponseDto.toDTO(user.getKeywords(), true);
        }
        if (!keywordRepository.existsByName(keywordName)) {
            log.info("Keyword '{}' not found, creating new one.", keywordName);
            keywordRepository.save(new Keyword(keywordName));
        }
        Keyword keyword = keywordRepository.findByName(keywordName);

        user.addKeyword(keyword);

        log.info("Successfully added keyword '{}' for user ID: {}", keywordName, user.getId());
        return KeywordAddResponseDto.toDTO(user.getKeywords());
    }

    @Transactional
    public KeywordResponseDto deleteKeyword(String keywordName, String email) throws KeywordNotFoundException {
        log.info("Deleting keyword '{}' for a user.", keywordName);

        Keyword keyword = keywordRepository.findByName(keywordName);
        if (keyword == null) {
            throw new KeywordNotFoundException("Keyword to delete not found: " + keywordName);
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            log.error("Cannot delete keyword, user not found.");
            throw new IllegalStateException("User not found during keyword deletion.");
        }

        user.deleteKeyword(keyword);
        log.info("Successfully deleted keyword '{}' for user ID: {}", keywordName, user.getId());

        return KeywordResponseDto.toDTO(user.getKeywords());
    }
}