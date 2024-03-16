package com.SeeAndYouGo.SeeAndYouGo.Review.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter // setter를 열어줘야 formdata가 바인딩될 수 있다.
@NoArgsConstructor
public class ReviewRequestDto {
    @Builder
    private ReviewRequestDto(MultipartFile image, boolean anonymous, String restaurant, String dept, String menuName, Double rate, String writer, String nickName, String comment, String imgUrl) {
        this.image = image;
        this.anonymous = anonymous;
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
    private boolean anonymous;
    private MultipartFile image;

}
