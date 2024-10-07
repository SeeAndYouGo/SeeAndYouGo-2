package com.SeeAndYouGo.SeeAndYouGo.Review;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Builder
@Getter
@RequiredArgsConstructor
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
