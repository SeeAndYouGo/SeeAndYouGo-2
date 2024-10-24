package com.SeeAndYouGo.SeeAndYouGo.keyword;

import com.SeeAndYouGo.SeeAndYouGo.keyword.dto.KeywordAddResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.keyword.dto.KeywordRequestDto;
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
        KeywordRequestDto keywordRequestDto = getKeywordRequestDto("돈까스");

        // when
        KeywordAddResponseDto keywordsByUser = keywordController.addKeyword(keywordRequestDto);


        // then
        Assert.assertTrue(keywordsByUser.getKeywords().stream().anyMatch(keyword -> keyword.equals("돈까스")));
        // 1. KeywordAddResponseDto에 등록한 키워드가 있어야함.
        // 2. User의 키워드 조회 시, 해당 키워드가 등록되어 있어야함.
        // 3. 키워드는 10개를 초과하지 않아야함.
        // 4.


    }

    private KeywordRequestDto getKeywordRequestDto(String keyword) {
        KeywordRequestDto keywordRequestDto = new KeywordRequestDto();
        keywordRequestDto.setKeyword(keyword);
        keywordRequestDto.setUser_id("test");

        return keywordRequestDto;
    }

}
