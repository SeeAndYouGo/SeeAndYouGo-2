package com.SeeAndYouGo.SeeAndYouGo.Review;

import com.SeeAndYouGo.SeeAndYouGo.Menu.Dept;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDto {
    private String restaurant;
    private String writer;
    private Dept dept;
    private String madeTime;
    private String comment;
    private String imgLink;
    private Double rate;

    public ReviewDto(String restaurant, String writer, Dept dept, String madeTime, String comment, String imgLink, Double rate) {
        this.restaurant = restaurant;
        this.writer = writer;
        this.dept = dept;
        this.madeTime = madeTime;
        this.comment = comment;
        this.imgLink = imgLink;
        this.rate = rate;
    }
    public static ReviewDto of(Review review) {
        return new ReviewDto(review.getRestaurant().getName(), review.writer, review.menu.getDept(),
                review.madeTime.toString(), review.comment, review.imgLink, review.reviewRate);
    }
}
