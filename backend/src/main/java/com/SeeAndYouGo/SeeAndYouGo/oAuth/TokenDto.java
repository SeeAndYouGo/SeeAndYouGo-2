package com.SeeAndYouGo.SeeAndYouGo.oAuth;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenDto {
    private String token;
    private String refreshToken;
    @Setter
    private String message; // login, join 중 하나

    @Builder
    public TokenDto(String token, String refreshToken, String message) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.message = message;
    }
}