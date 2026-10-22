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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users")
public class User {
    public static final int NICKNAME_CHANGE_INTERVAL_DAYS = 14;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    private String email;
    private String nickname;
    @Enumerated(EnumType.STRING)
    private Social socialType; // Kakao, Naver, Google...
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    private UserType userType;

    @CreationTimestamp // INSERT 시 자동으로 값을 채워줌
    private LocalDateTime createTime = LocalDateTime.now();

    @UpdateTimestamp
    private LocalDateTime lastUpdateTime = LocalDateTime.now();

    private LocalDateTime nicknameUpdatedAt; // 마지막 닉네임 변경 시각. 변경 이력이 없으면 null

    private String refreshToken;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserKeyword> userKeywords = new ArrayList<>();

    @Builder
    public User(String email, String nickname, Social socialType, UserType userType) {
        this.email = email;
        this.nickname = nickname;
        this.socialType = socialType;
        this.userType = userType == null ? UserType.USER : userType;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
        this.nicknameUpdatedAt = LocalDateTime.now();
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public UserType getUserType() {
        return userType == null ? UserType.USER : userType;
    }

    public boolean isAdmin() {
        return getUserType() == UserType.ADMIN;
    }

    public void changeUserType(UserType userType) {
        this.userType = userType == null ? UserType.USER : userType;
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
        // 아직 닉네임을 지정하지 않은 최초 등록이거나, 변경 이력이 없거나, 마지막 변경일로부터 14일이 지났으면 수정할 수 있다.
        if (nickname == null || nicknameUpdatedAt == null) {
            return true;
        }
        long daysSinceLastChange = ChronoUnit.DAYS.between(nicknameUpdatedAt.toLocalDate(), dateTime.toLocalDate());
        return daysSinceLastChange >= NICKNAME_CHANGE_INTERVAL_DAYS;
    }
}
