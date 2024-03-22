package com.SeeAndYouGo.SeeAndYouGo.Review.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewRequestDto {
    private String restaurant;
    private String dept;
    private String menuName;
    private Double rate;
    private String writer;
    private String nickName;
    private String comment;
    private boolean anonymous;
}
