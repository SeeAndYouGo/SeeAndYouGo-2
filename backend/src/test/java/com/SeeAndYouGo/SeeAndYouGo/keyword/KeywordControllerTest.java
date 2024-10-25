package com.SeeAndYouGo.SeeAndYouGo.keyword;

import com.SeeAndYouGo.SeeAndYouGo.keyword.dto.KeywordAddResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.keyword.dto.KeywordRequestDto;
import com.SeeAndYouGo.SeeAndYouGo.keyword.dto.KeywordResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.TokenProvider;
import com.SeeAndYouGo.SeeAndYouGo.user.Social;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import com.SeeAndYouGo.SeeAndYouGo.user.UserRepository;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@Transactional
public class KeywordControllerTest {
    @Autowired
    private KeywordController keywordController;

    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private TokenProvider tokenProvider;

    @BeforeEach
    public void init(){
        // decodeToEmail 수동 설정.
        Mockito.doReturn("test@daum.net")
                .when(tokenProvider)
                .decodeToEmail(any(String.class));

        User user = User.builder()
                        .email("test@daum.net")
                        .nickname("test")
                        .socialType(Social.KAKAO)
                        .build();

        userRepository.save(user);
    }

    @DisplayName("키워드_등록하기")
    @Test
    void 키워드_등록하기() throws Exception {
        // given
        String keyword = "돈까스";
        KeywordRequestDto keywordRequestDto = getKeywordRequestDto(keyword);

        // when
        KeywordAddResponseDto keywordsByUser = keywordController.addKeyword(keywordRequestDto);

        // then
        // 1. KeywordAddResponseDto에 등록한 키워드가 있어야함.
        Assert.assertTrue(keywordsByUser.getKeywords().stream().anyMatch(keywordByUser -> keywordByUser.equals(keyword)));

        // 2. User의 키워드 조회 시, 해당 키워드가 등록되어 있어야함.
        User user = userRepository.findByEmail("test@daum.net");
        Assert.assertTrue(user.getKeywords().stream().map(Keyword::getName).anyMatch(keywordByUser -> keywordByUser.equals(keyword)));
    }

    @DisplayName("키워드 초과 테스트")
    @Test
    void 키워드_초과() throws Exception {
        // given
        setKeywordExceed();
        KeywordRequestDto keywordRequestDto = getKeywordRequestDto("김치찌개");

        // when
        KeywordAddResponseDto keywordsByUser = keywordController.addKeyword(keywordRequestDto);

        // then
        // 1. response에 isExceed가 true여야함.

        // 2. 김치찌개 라는 키워드는 등록되지 않아야함.
        Assert.assertFalse(keywordsByUser.getKeywords().stream().anyMatch(keyword -> keyword.equals("김치찌개")));
        Assert.assertEquals(keywordsByUser.getIsExceed(), true);

        User user = userRepository.findByEmail("test@daum.net");
        Assert.assertFalse(user.getKeywords().stream().map(Keyword::getName).anyMatch(keyword -> keyword.equals("돈까스")));
    }

    @DisplayName("중복 키워드 테스트")
    @Test
    void 키워드_중복() throws Exception {
        // given
        String keyword = "돈까스";
        KeywordRequestDto keywordRequestDto = getKeywordRequestDto(keyword);
        keywordController.addKeyword(keywordRequestDto);

        // when
        KeywordAddResponseDto keywordsByUser = keywordController.addKeyword(keywordRequestDto);

        // then
        // 같은 키워드가 2개이지 않아야함.
        List<String> matched = keywordsByUser.getKeywords().stream().filter(keywordByUser -> keywordByUser.equals(keyword)).collect(Collectors.toList());
        Assert.assertEquals(matched.size(), 1);

        User user = userRepository.findByEmail("test@daum.net");
        List<String> matchedByUser = user.getKeywords().stream().map(Keyword::getName).filter(keywordByUser -> keywordByUser.equals(keyword)).collect(Collectors.toList());
        Assert.assertEquals(matchedByUser.size(), 1);
    }

    @DisplayName("키워드 삭제 테스트")
    @Test
    void 키워드_삭제() throws Exception {
        // given
        String keyword = "돈까스";
        KeywordRequestDto keywordRequestDto = getKeywordRequestDto(keyword);
        keywordController.addKeyword(keywordRequestDto);

        // when
        KeywordResponseDto keywordsByUser = keywordController.deleteKeyword(keywordRequestDto);

        // then
        // 1. keywordResponseDto에 '돈까스'라는 키워드가 없어야함.
        List<String> matched = keywordsByUser.getKeywords().stream().filter(keywordByUser -> keywordByUser.equals(keyword)).collect(Collectors.toList());
        Assert.assertEquals(matched.size(), 0);

        // 2. user를 조회했을 때, 나오는 키워드 리스트에 '돈까스'라는 키워드가 없어야함.
        User user = userRepository.findByEmail("test@daum.net");
        List<String> matchedByUser = user.getKeywords().stream().map(Keyword::getName).filter(keywordByUser -> keywordByUser.equals(keyword)).collect(Collectors.toList());
        Assert.assertEquals(matchedByUser.size(), 0);
    }


    private void setKeywordExceed() {
        // 키워드 10개로 세팅하기.
        StringBuilder sb = new StringBuilder();
        sb.append("돈까스");

        for (int i = 0; i < 10; i++) {
            sb.append(i);
            KeywordRequestDto keywordRequestDto = getKeywordRequestDto(sb.toString());
            keywordController.addKeyword(keywordRequestDto);
        }
    }

    private KeywordRequestDto getKeywordRequestDto(String keyword) {
        KeywordRequestDto keywordRequestDto = new KeywordRequestDto();
        keywordRequestDto.setKeyword(keyword);
        keywordRequestDto.setUser_id("test");

        return keywordRequestDto;
    }

}
