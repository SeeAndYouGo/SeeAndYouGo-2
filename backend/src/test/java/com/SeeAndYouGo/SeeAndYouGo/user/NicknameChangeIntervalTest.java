package com.SeeAndYouGo.SeeAndYouGo.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 닉네임은 마지막 변경일로부터 14일이 지나야 다시 변경할 수 있다.
 */
@DisplayName("닉네임 변경 주기")
class NicknameChangeIntervalTest {

    private static final LocalDateTime LAST_UPDATE = LocalDateTime.of(2026, 9, 1, 10, 0);

    @Test
    @DisplayName("14일이 지나지 않았으면 변경할 수 없다")
    void within14Days() {
        assertThat(userUpdatedAt(LAST_UPDATE).canUpdateNickname(LAST_UPDATE.plusDays(13))).isFalse();
    }

    @Test
    @DisplayName("14일이 지났으면 변경할 수 있다")
    void after14Days() {
        assertThat(userUpdatedAt(LAST_UPDATE).canUpdateNickname(LAST_UPDATE.plusDays(14))).isTrue();
    }

    @Test
    @DisplayName("한 달 넘게 지났으면 날짜(일)와 상관없이 변경할 수 있다")
    void afterMonths() {
        // 1개월 3일: 예전 계산은 일(3)만 비교해 변경 불가로 판단했다
        assertThat(userUpdatedAt(LAST_UPDATE).canUpdateNickname(LAST_UPDATE.plusMonths(1).plusDays(3))).isTrue();
        // 1년 뒤 같은 날: 예전 계산은 연도를 무시해 변경 불가로 판단했다
        assertThat(userUpdatedAt(LAST_UPDATE).canUpdateNickname(LAST_UPDATE.plusYears(1))).isTrue();
    }

    @Test
    @DisplayName("닉네임을 아직 정하지 않았으면 바로 변경할 수 있다")
    void firstNickname() {
        User user = User.builder().email("user@test.com").build();
        ReflectionTestUtils.setField(user, "lastUpdateTime", LAST_UPDATE);

        assertThat(user.canUpdateNickname(LAST_UPDATE)).isTrue();
    }

    private User userUpdatedAt(LocalDateTime lastUpdateTime) {
        User user = User.builder().email("user@test.com").nickname("배고픈너구리").build();
        ReflectionTestUtils.setField(user, "lastUpdateTime", lastUpdateTime);
        return user;
    }
}
