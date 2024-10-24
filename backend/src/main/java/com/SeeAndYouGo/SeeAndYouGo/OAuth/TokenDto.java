package com.SeeAndYouGo.SeeAndYouGo.oAuth;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenDto {
    private String token;
    private String message; // login, join 중 하나

    @Builder
    public TokenDto(String token, String message) {
        this.token = token;
        this.message = message;
    }
}
