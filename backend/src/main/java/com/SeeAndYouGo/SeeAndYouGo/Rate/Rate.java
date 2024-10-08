package com.SeeAndYouGo.SeeAndYouGo.Rate;

import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rate_id")
    private Long id;

    @Enumerated(value = EnumType.STRING)
    private Restaurant restaurant;

    @ColumnDefault("0.0")
    private double rate = 0.0;

    @Column(columnDefinition = "integer default 0")
    private Integer reflectedNumber = 0;

    @Builder
    public Rate(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public void reflectRate(double rate){
        double totalSum = this.rate * reflectedNumber;
        double updateSum = totalSum + rate;

        this.reflectedNumber++;
        this.rate = updateSum/reflectedNumber;
    }

    public void exceptRate(double rate) {
        double totalSum = this.rate * reflectedNumber;
        double updateSum = totalSum - rate;

        this.reflectedNumber--;
        this.rate = updateSum / reflectedNumber;
    }
}
