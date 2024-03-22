package com.SeeAndYouGo.SeeAndYouGo.Review;

import com.SeeAndYouGo.SeeAndYouGo.Menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.Review.dto.ReviewRequestDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {
    @Builder
    public Review(Long id, String writerEmail, String writerNickname, String madeTime, Integer likeCount, Menu menu, String comment, String imgLink, Double reviewRate, Restaurant restaurant, Integer reportCount) {
        this.id = id;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    private String writerEmail;
    private String writerNickname;

    private String madeTime;

    private Integer likeCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    private Menu menu;

    private String comment;

    private String imgLink;

    private Double reviewRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    private Integer reportCount = 0;

    public Integer incrementReportCount(){
        reportCount++;
        return reportCount;
    }

    public Integer incrementLikeCount(){
        likeCount++;
        return likeCount;
    }

    public Integer decrementLikeCount(){
        likeCount--;
        return likeCount;
    }

    public static Review createEntity(ReviewData data, Restaurant restaurant, Menu menu, String time) {
        return Review.builder()
                .writerEmail(data.getEmail())
                .writerNickname(data.getNickName())
                .madeTime(time)
                .likeCount(0)
                .comment(data.getComment())
                .imgLink(data.getImgUrl())
                .reviewRate(data.getRate())
                .reportCount(0)
                .restaurant(restaurant)
                .menu(menu)
                .build();
    }
}