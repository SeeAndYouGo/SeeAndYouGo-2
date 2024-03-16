package com.SeeAndYouGo.SeeAndYouGo.Review.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewRequestDto { // review 등록 때, 프론트에서 json과 image를 넘겨준다면, 쓸 DTO

    @Builder
    private ReviewRequestDto(String restaurant, String dept, String menuName, Double rate, String writer, String nickName, String comment, String imgUrl) {
        this.restaurant = restaurant;
        this.dept = dept;
        this.menuName = menuName;
        this.rate = rate;
        this.writer = writer;
        this.nickName = nickName;
        this.comment = comment;
        this.imgUrl = imgUrl;
    }

    private String restaurant;
    private String dept;
    private String menuName;
    private Double rate;
    private String writer;
    private String nickName;
    private String comment;
    private String imgUrl;
}
