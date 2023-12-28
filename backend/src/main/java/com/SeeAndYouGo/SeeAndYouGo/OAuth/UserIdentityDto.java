package com.SeeAndYouGo.SeeAndYouGo.OAuth;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class UserIdentityDto {
    private final String id;
}
