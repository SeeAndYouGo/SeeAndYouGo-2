package com.SeeAndYouGo.SeeAndYouGo.Review;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReviewRequestDto {
    public String restaurant;
    public String writer;
    public String comment;
    public Double rate;

    public ReviewRequestDto(String restaurant, String writer, String comment, Double rate) {
        this.restaurant = restaurant;
        this.writer = writer;
        this.comment = comment;
        this.rate = rate;
    }
    public static ReviewDto of(Review review) {
        return new ReviewDto(review.getRestaurant().getName(), review.writer,
                review.madeTime.toString(), review.comment, review.imgLink, review.reviewRate);
    }
}
