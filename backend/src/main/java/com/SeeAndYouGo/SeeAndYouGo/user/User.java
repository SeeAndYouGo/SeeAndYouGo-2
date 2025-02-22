package com.SeeAndYouGo.SeeAndYouGo.user;


import com.SeeAndYouGo.SeeAndYouGo.keyword.Keyword;
import com.SeeAndYouGo.SeeAndYouGo.userKeyword.UserKeyword;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    private String email;
    private String nickname;
    @Enumerated(EnumType.STRING)
    private Social socialType; // Kakao, Naver, Google...

    @CreationTimestamp // INSERT 시 자동으로 값을 채워줌
    private LocalDateTime createTime = LocalDateTime.now();

    @UpdateTimestamp
    private LocalDateTime lastUpdateTime = LocalDateTime.now();

    private String refreshToken;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
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

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void addKeyword(Keyword keyword){
        if (!this.existsKeyword(keyword)) {
            UserKeyword userKeyword = new UserKeyword(this, keyword);
            this.userKeywords.add(userKeyword);
            keyword.addUserKeyword(userKeyword);
        }
    }

    public void deleteKeyword(Keyword keyword){
        for(UserKeyword userKeyword : this.userKeywords){
            if (userKeyword.getKeyword().getName().equals(keyword.getName()))  {
                this.userKeywords.remove(userKeyword);
                break;
            }
        }
    }

    private boolean existsKeyword(Keyword keyword) {
        for (UserKeyword userKeyword : this.getUserKeywords()){
            if (keyword.getName().equals(userKeyword.getKeyword().getName()))
                return true;
        }
        return false;
    }

    public List<Keyword> getKeywords() {
        return this.getUserKeywords().stream().map(UserKeyword::getKeyword).collect(Collectors.toList());
    }

    /**
     * dateTime를 기준으로 닉네임을 변경할 수 있는지 판단
     * @param dateTime 판단 날짜(통상 오늘)
     * @return 닉네임 변경 가능 여부
     */
    public boolean canUpdateNickname(LocalDateTime dateTime) {


        Period period = Period.between(lastUpdateTime.toLocalDate(), dateTime.toLocalDate());

        int dateGap = Math.abs(period.getDays());
        int monthGap = Math.abs(period.getMonths());

        // 14일 이후에 변경하는 경우나 아직 닉네임을 지정하지 않아 최초 등록의 경우는 닉네임을 수정할 수 있다.
        return (dateGap > 14) || (monthGap > 2)  || nickname == null;
    }
}
