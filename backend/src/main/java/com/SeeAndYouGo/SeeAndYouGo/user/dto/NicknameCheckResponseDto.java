package com.SeeAndYouGo.SeeAndYouGo.user.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NicknameCheckResponseDto {
    private final Boolean redundancy;
}
