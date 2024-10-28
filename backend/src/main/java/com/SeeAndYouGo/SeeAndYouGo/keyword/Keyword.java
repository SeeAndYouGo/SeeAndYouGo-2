package com.SeeAndYouGo.SeeAndYouGo.keyword;

import com.SeeAndYouGo.SeeAndYouGo.user.User;
import com.SeeAndYouGo.SeeAndYouGo.userKeyword.UserKeyword;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity @Getter
@NoArgsConstructor
public class Keyword {
    @Id @Column(name = "keyword_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "keyword")
    private List<UserKeyword> userKeywords = new ArrayList<>();

    @Builder
    public Keyword(String name) {
        this.name = name;
    }

    public void addUserKeyword(UserKeyword userKeyword) {
        this.userKeywords.add(userKeyword);
    }
}