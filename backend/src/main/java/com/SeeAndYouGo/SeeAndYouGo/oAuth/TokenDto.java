package com.SeeAndYouGo.SeeAndYouGo.oAuth;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenDto {
    private String token;
    private String refreshToken;
    private String userType;
    @Setter
    private String message; // login, join 중 하나

    @Builder
    public TokenDto(String token, String refreshToken, String userType, String message) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userType = userType;
        this.message = message;
    }
}
