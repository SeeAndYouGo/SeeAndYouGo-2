package com.SeeAndYouGo.SeeAndYouGo.user;


import com.SeeAndYouGo.SeeAndYouGo.Keyword.Keyword;
import com.SeeAndYouGo.SeeAndYouGo.Keyword.UserKeyword;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public void addKeyword(Keyword keyword){
        if (!this.existsKeyword(keyword))
            this.userKeywords.add(new UserKeyword(this, keyword));
    }

    public void deleteKeyword(Keyword keyword){
        for(UserKeyword userKeyword : this.userKeywords){
            Keyword existingKeyword = userKeyword.getKeyword();
            if (existingKeyword == keyword)  {
                this.userKeywords.remove(userKeyword);
                break;
            }
        }
    }

    private boolean existsKeyword(Keyword keyword) {
        for (UserKeyword userKeyword : this.getUserKeywords()){
            if (keyword == userKeyword.getKeyword())
                return true;
        }
        return false;
    }

    public List<Keyword> getKeywords() {
        return this.getUserKeywords().stream().map(x -> x.getKeyword()).collect(Collectors.toList());
    }
}