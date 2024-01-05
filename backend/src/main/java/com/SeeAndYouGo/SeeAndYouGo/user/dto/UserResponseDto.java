package com.SeeAndYouGo.SeeAndYouGo.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponseDto {
    private final String nickname;
}
