package com.SeeAndYouGo.SeeAndYouGo.user;


import com.SeeAndYouGo.SeeAndYouGo.Keyword.UserKeyword;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserKeyword> userKeywords = new ArrayList<>();

    @Builder
    public User(String email, String nickname, Social socialType) {
        this.email = email;
        this.nickname = nickname;
        this.socialType = socialType;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }
}
