package com.SeeAndYouGo.SeeAndYouGo.Review;

import com.SeeAndYouGo.SeeAndYouGo.Menu.Dept;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDto {
    private Long Id;
    private String restaurant;
    private String writer;
    private Dept dept;
    private String menuName;
    private String madeTime;
    private String comment;
    private String imgLink;
    private Double rate;

    public ReviewDto(Long id, String restaurant, String writer, Dept dept, String menuName, String madeTime, String comment, String imgLink, Double rate) {
        this.Id = id;
        this.restaurant = restaurant;
        this.writer = writer;
        this.dept = dept;
        this.menuName = menuName;
        this.madeTime = madeTime;
        this.comment = comment;
        this.imgLink = imgLink;
        this.rate = rate;
    }
    public static ReviewDto of(Review review) {
        return new ReviewDto(review.getId(), review.getRestaurant().getName(), review.writer, review.menu.getDept(),
                review.getMenu().getMenuName(), review.madeTime.toString(), review.comment, review.imgLink, review.reviewRate);
    }
}
