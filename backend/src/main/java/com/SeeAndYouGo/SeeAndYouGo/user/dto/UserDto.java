package com.SeeAndYouGo.SeeAndYouGo.user.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UserDto { // 이건 왜 있지...?
    private final String socialId;

    private final String email;
    private final String nickname;


}
