package com.SeeAndYouGo.SeeAndYouGo.userKeyword;

import com.SeeAndYouGo.SeeAndYouGo.keyword.Keyword;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity @Getter
@NoArgsConstructor
public class UserKeyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id")
    private Keyword keyword;

    public UserKeyword(User user, Keyword keyword) {
        this.user = user;
        this.keyword = keyword;
    }
}