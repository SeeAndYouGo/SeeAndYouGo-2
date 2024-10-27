package com.SeeAndYouGo.SeeAndYouGo.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class UserIdentityDto {
    private final String id;
    private final String email;
}
