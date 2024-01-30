package com.SeeAndYouGo.SeeAndYouGo.like;

import com.SeeAndYouGo.SeeAndYouGo.Review.Review;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "review_id")
    private Review review;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Like(Review review, User user) {
        this.review = review;
        this.user = user;
    }
}
