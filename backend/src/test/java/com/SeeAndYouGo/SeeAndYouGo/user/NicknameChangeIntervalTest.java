package com.SeeAndYouGo.SeeAndYouGo.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 닉네임은 마지막 닉네임 변경일로부터 14일이 지나야 다시 변경할 수 있다.
 * 로그인 등으로 갱신되는 lastUpdateTime과는 무관하다.
 */
@DisplayName("닉네임 변경 주기")
class NicknameChangeIntervalTest {

    private static final LocalDateTime LAST_CHANGE = LocalDateTime.of(2026, 9, 1, 10, 0);

    @Test
    @DisplayName("14일이 지나지 않았으면 변경할 수 없다")
    void within14Days() {
        assertThat(userChangedAt(LAST_CHANGE).canUpdateNickname(LAST_CHANGE.plusDays(13))).isFalse();
    }

    @Test
    @DisplayName("14일이 지났으면 변경할 수 있다")
    void after14Days() {
        assertThat(userChangedAt(LAST_CHANGE).canUpdateNickname(LAST_CHANGE.plusDays(14))).isTrue();
    }

    @Test
    @DisplayName("한 달 넘게 지났으면 날짜(일)와 상관없이 변경할 수 있다")
    void afterMonths() {
        // 1개월 3일: 예전 계산은 일(3)만 비교해 변경 불가로 판단했다
        assertThat(userChangedAt(LAST_CHANGE).canUpdateNickname(LAST_CHANGE.plusMonths(1).plusDays(3))).isTrue();
        // 1년 뒤 같은 날: 예전 계산은 연도를 무시해 변경 불가로 판단했다
        assertThat(userChangedAt(LAST_CHANGE).canUpdateNickname(LAST_CHANGE.plusYears(1))).isTrue();
    }

    @Test
    @DisplayName("닉네임을 아직 정하지 않았으면 바로 변경할 수 있다")
    void firstNickname() {
        User user = User.builder().email("user@test.com").build();

        assertThat(user.canUpdateNickname(LAST_CHANGE)).isTrue();
    }

    @Test
    @DisplayName("닉네임 변경 이력이 없으면 바로 변경할 수 있다")
    void noChangeHistory() {
        User user = User.builder().email("user@test.com").nickname("배고픈너구리").build();
        ReflectionTestUtils.setField(user, "lastUpdateTime", LAST_CHANGE);

        assertThat(user.canUpdateNickname(LAST_CHANGE)).isTrue();
    }

    @Test
    @DisplayName("닉네임을 변경하면 그 시각부터 14일 동안 다시 변경할 수 없다")
    void justChanged() {
        User user = User.builder().email("user@test.com").nickname("배고픈너구리").build();
        user.changeNickname("배부른너구리");

        assertThat(user.canUpdateNickname(LocalDateTime.now())).isFalse();
    }

    private User userChangedAt(LocalDateTime nicknameUpdatedAt) {
        User user = User.builder().email("user@test.com").nickname("배고픈너구리").build();
        ReflectionTestUtils.setField(user, "nicknameUpdatedAt", nicknameUpdatedAt);
        return user;
    }
}
