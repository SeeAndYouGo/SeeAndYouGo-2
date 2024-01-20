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

    @ColumnDefault("0")
    public Integer reportCount = 0;

    @Builder
    public ReviewHistory(Long id, Long reviewId, String writerEmail, String writerNickname, String madeTime,
                         Integer likeCount, Menu menu, String comment, String imgLink, Double reviewRate,
                         Restaurant restaurant, Integer reportCount) {
        this.id = id;
        this.reviewId = reviewId;
        this.writerEmail = writerEmail;
        this.writerNickname = writerNickname;
        this.madeTime = madeTime;
        this.likeCount = likeCount;
        this.menu = menu;
        this.comment = comment;
        this.imgLink = imgLink;
        this.reviewRate = reviewRate;
        this.restaurant = restaurant;
        this.reportCount = reportCount;
    }
}
