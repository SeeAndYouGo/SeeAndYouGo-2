package com.SeeAndYouGo.SeeAndYouGo.like.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LikeResponseDto {
    private boolean isLike;

    @Builder
    public LikeResponseDto(boolean isLike) {
        this.isLike = isLike;
    }
}
