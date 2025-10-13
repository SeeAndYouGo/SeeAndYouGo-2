package com.SeeAndYouGo.SeeAndYouGo.review;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Builder
@Getter
@RequiredArgsConstructor
@ToString
public class ReviewData {
    private final String restaurant;
    private final Long menuId;
    private final String dept;
    private final String menuName;
    private final Double rate;
    private final String email;
    private final String nickName;
    private final String comment;
    private final String imgUrl;
}