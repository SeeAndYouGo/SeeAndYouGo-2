package com.SeeAndYouGo.SeeAndYouGo.like.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter
public class LikeResponseDto {
    private boolean like;
    private boolean mine;

    @Builder
    public LikeResponseDto(boolean like, boolean mine) {
        this.like = like;
        this.mine = mine;
    }
}
