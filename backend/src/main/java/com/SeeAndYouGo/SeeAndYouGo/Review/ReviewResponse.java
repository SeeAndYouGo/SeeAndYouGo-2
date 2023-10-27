package com.SeeAndYouGo.SeeAndYouGo.Review;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewResponse {
    private String restaurant;
    private String writer;
    private String madeTime;
    private String comment;
    private String image;
    private Double rate;

}