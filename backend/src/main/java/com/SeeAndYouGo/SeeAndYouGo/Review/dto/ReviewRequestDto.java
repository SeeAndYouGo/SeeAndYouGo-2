package com.SeeAndYouGo.SeeAndYouGo.Review.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewRequestDto {
    private String restaurant;
    private Long menuId; // 어떤 메뉴에 해당하는 리뷰인지 판별하기 위해 추가
    private String dept;
    private String menuName;
    private Double rate;
    private String writer;
    private String nickName;
    private String comment;
    private boolean anonymous;
}
