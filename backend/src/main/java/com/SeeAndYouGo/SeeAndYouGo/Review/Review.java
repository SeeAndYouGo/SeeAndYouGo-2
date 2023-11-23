package com.SeeAndYouGo.SeeAndYouGo.Review;

import com.SeeAndYouGo.SeeAndYouGo.Menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Getter
@Setter
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    public Long id;
    @Column(name = "writer")
    public String writer;
    @Column(name = "madeTime")
    public String madeTime;
    @Column(name = "likeCount")
    public Integer likeCount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    public Menu menu;
    @Column(name = "comment")
    public String comment;
    @Column(name = "imgLink")
    public String imgLink;
    @Column(name = "reviewRate")
    public Double reviewRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    public Restaurant restaurant;

//    public void setMadeTime(String madeTimeStr) {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        this.madeTime = LocalDateTime.parse(madeTimeStr, formatter).toString();
//    }
}
