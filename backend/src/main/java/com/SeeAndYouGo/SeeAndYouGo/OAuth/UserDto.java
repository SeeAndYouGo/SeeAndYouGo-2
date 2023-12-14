package com.SeeAndYouGo.SeeAndYouGo.OAuth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UserDto {
    private final String socialId;

    private final String email;
    private final String nickname;


}
