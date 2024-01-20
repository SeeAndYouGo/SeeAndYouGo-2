package com.SeeAndYouGo.SeeAndYouGo.Review.dto;

import com.SeeAndYouGo.SeeAndYouGo.Menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.Review.Review;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@RequiredArgsConstructor
public class ReviewDto {
    private final Long reviewId;
    private final String restaurant;
    private final String writer;
    private final Dept dept;
    private final String menuName;
    private final String madeTime;
    private final String comment;
    private final String imgLink;
    private final Double rate;

    public static ReviewDto of(Review review) {
        return new ReviewDto(review.getId(), review.getRestaurant().getName(), review.writerNickname, review.menu.getDept(),
                review.getMenu().getMenuName(), review.madeTime.toString(), review.comment, review.imgLink, review.reviewRate);
    }
}
