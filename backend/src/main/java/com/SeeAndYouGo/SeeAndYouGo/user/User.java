package com.SeeAndYouGo.SeeAndYouGo.user;


import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    private String email;
    private String nickname;
    @Enumerated(EnumType.STRING)
    private Social socialType; // Kakao, Naver, Google...

    public User(String email, String nickname, Social socialType) {
        this.email = email;
        this.nickname = nickname;
        this.socialType = socialType;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }
}
