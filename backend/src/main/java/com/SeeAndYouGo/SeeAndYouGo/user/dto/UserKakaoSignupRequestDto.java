package com.SeeAndYouGo.SeeAndYouGo.user.dto;


import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserKakaoSignupRequestDto { // 얜 왜 있지...?
    private final String kakaoId; // kakao에서 받은 user의 identifier에 해당
    private final String email;
    private final String nickname;
}
