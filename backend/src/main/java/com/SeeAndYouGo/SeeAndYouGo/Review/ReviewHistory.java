package com.SeeAndYouGo.SeeAndYouGo.Review;

import com.SeeAndYouGo.SeeAndYouGo.Menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class ReviewHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_history_id")
    public Long id;

    public Long reviewId;

    public String writerEmail;
    public String writerNickname;


    public String madeTime;

    public Integer likeCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    public Menu menu;

    public String comment;

    public String imgLink;

    public Double reviewRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    public Restaurant restaurant;

    public Integer reportCount;

    public ReviewHistory(Review review){
        this.reviewId = review.getId();
        this.writerEmail = review.getWriterEmail();
        this.writerNickname = review.getWriterNickname();
        this.madeTime = review.getMadeTime();
        this.likeCount = review.getLikeCount();
        this.menu = review.getMenu();
        this.comment = review.getComment();
        this.imgLink = review.getImgLink();
        this.reviewRate = review.getReviewRate();
        this.restaurant = review.getRestaurant();
        this.reportCount = review.getReportCount();
    }
}
