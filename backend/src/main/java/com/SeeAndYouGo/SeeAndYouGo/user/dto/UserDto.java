package com.SeeAndYouGo.SeeAndYouGo.user.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UserDto {
    private final String socialId;

    private final String email;
    private final String nickname;


}
