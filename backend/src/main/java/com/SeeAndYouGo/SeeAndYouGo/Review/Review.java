package com.SeeAndYouGo.SeeAndYouGo.Review;

import com.SeeAndYouGo.SeeAndYouGo.Menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

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

    public String writer;

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

    public Integer incrementReportCount(){
        reportCount++;
        return reportCount;
    }

//    public void setMadeTime(String madeTimeStr) {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        this.madeTime = LocalDateTime.parse(madeTimeStr, formatter).toString();
//    }
}
