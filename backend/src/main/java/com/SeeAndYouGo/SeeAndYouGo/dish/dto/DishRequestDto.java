package com.SeeAndYouGo.SeeAndYouGo.dish.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DishRequestDto {
    private long id;
    private String changeName;
}
