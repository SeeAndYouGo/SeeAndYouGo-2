package com.SeeAndYouGo.SeeAndYouGo.Review.dto;

import com.SeeAndYouGo.SeeAndYouGo.Menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.Menu.MenuType;
import com.SeeAndYouGo.SeeAndYouGo.Review.Review;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
public class ReviewResponseDto {
    private Long reviewId;
     private String restaurant;
    private String writer;
    private Dept dept;
    private String menuType;
    private List<String> mainDishList;
    private String madeTime;
    private String comment;
    private String imgLink;
    private Double rate;
    private boolean isLike;
    private Integer likeCount;

    public ReviewResponseDto(Review review, boolean isLike) {
        this.reviewId = review.getId();
        this.restaurant = review.getRestaurant().toString();
        this.writer = review.getWriterNickname();
        this.dept = review.getMenu().getDept();
        this.menuType = review.getMenu().getMenuType().toString();
        this.mainDishList = review.getMenu().getMainDishToString();
        this.madeTime = review.getMadeTime();
        this.comment = review.getComment();
        this.imgLink = review.getImgLink();
        this.rate = review.getReviewRate();
        this.isLike = isLike;
        this.likeCount = review.getLikeCount();
    }
}
