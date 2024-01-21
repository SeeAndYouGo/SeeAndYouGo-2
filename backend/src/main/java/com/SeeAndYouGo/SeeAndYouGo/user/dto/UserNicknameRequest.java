package com.SeeAndYouGo.SeeAndYouGo.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserNicknameRequest {
    private String token;
    private String nickname;

    @Builder
    public UserNicknameRequest(String token, String nickname) {
        this.token = token;
        this.nickname = nickname;
    }
}
