package com.SeeAndYouGo.SeeAndYouGo.Review.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReviewRequestDto { // review 등록 때, 프론트에서 json과 image를 넘겨준다면, 쓸 DTO
    public String restaurant;
    public String dept;
    public String menuName;
    public Double rate;
    public String writer;
    public String comment;
}
