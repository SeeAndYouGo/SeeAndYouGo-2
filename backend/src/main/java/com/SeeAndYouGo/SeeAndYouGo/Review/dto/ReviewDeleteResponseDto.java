package com.SeeAndYouGo.SeeAndYouGo.Review.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewDeleteResponseDto {
    private boolean success;

    @Builder
    public ReviewDeleteResponseDto(boolean success) {
        this.success = success;
    }
}
