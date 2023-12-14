package com.SeeAndYouGo.SeeAndYouGo.OAuth;


import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String nickname;
    private String socialId;  // kakao identifier
    private Social socialType; // Kakao, Naver, Google...

    public User(String email, String nickname, String socialId, Social socialType) {
        this.email = email;
        this.nickname = nickname;
        this.socialId = socialId;
        this.socialType = socialType;
    }
}
