package com.SeeAndYouGo.SeeAndYouGo.visitor;

import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.TokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.LocalDate;

import static com.SeeAndYouGo.SeeAndYouGo.config.Const.KEY_OF_TOKEN_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 테스트 목표: Redis 기반 방문자 카운팅(중복 제거) 로직 검증.
 *
 *  - 익명(IP 기준)
 *    * 첫 방문: today/total 모두 증가, IP 키만 10분 TTL 로 마킹 (user="unknown" 키는 마킹하지 않음)
 *    * 같은 IP 재방문: 증가 없음, IP 키 TTL 만 갱신
 *  - 로그인 사용자(이메일 기준)
 *    * 첫 방문: today/total 모두 증가, user 키 + IP 키 모두 마킹
 *    * 같은 user 재방문: 증가 없음, 두 키 모두 TTL 갱신
 *  - IP 추출 우선순위: X-Forwarded-For → ... → request.getRemoteAddr()
 *  - tokenId 헤더가 있으면 TokenProvider 로 이메일 디코딩
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // request.getHeader(...) 가 여러 헤더 키로 호출되어 strict-stub 와 충돌
@DisplayName("방문자 캐시 - VisitorInterceptor")
class VisitorInterceptorTest {

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOps;
    @Mock private HashOperations<String, String, String> hashOps;
    @Mock private TokenProvider tokenProvider;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    @InjectMocks private VisitorInterceptor interceptor;

    private static final String IP = "203.0.113.42";
    private static final String IP_KEY = Const.PREFIX_VISITOR_IP + IP;
    private static final String UNKNOWN_KEY = Const.PREFIX_VISITOR_USER + "unknown";

    @BeforeEach
    void setUp() {
        given(redisTemplate.opsForValue()).willReturn(valueOps);
    }

    // ===== 익명 방문자 =====

    @Test
    @DisplayName("익명 첫 방문: today/total 증가 + IP 키만 10분 TTL 로 마킹 (user=unknown 키는 마킹 안 함)")
    void anonymous_firstVisit_increasesAndMarksIpOnly() {
        // given - 헤더 없음, RemoteAddr 로 IP 결정, tokenId 없음
        given(request.getRemoteAddr()).willReturn(IP);
        given(request.getHeader(KEY_OF_TOKEN_ID)).willReturn(null);
        given(valueOps.get(UNKNOWN_KEY)).willReturn(null);
        given(valueOps.get(IP_KEY)).willReturn(null);
        org.mockito.Mockito.doReturn(hashOps).when(redisTemplate).opsForHash();

        // when
        boolean result = interceptor.preHandle(request, response, new Object());

        // then
        assertThat(result).isTrue();
        verify(hashOps).increment(Const.KEY_TODAY_VISITOR, LocalDate.now().toString(), 1L);
        verify(valueOps).increment(Const.KEY_TOTAL_VISITOR);
        verify(valueOps).set(IP_KEY, "1", Duration.ofMinutes(10));
        // "visitor:user:unknown" 은 set 하지 않음
        verify(valueOps, never()).set(eq(UNKNOWN_KEY), anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("익명 재방문(같은 IP): 증가 없음, IP 키 TTL 만 갱신")
    void anonymous_sameIp_doesNotIncrease() {
        given(request.getRemoteAddr()).willReturn(IP);
        given(request.getHeader(KEY_OF_TOKEN_ID)).willReturn(null);
        given(valueOps.get(UNKNOWN_KEY)).willReturn(null);
        given(valueOps.get(IP_KEY)).willReturn("1"); // 이미 방문 기록 있음

        interceptor.preHandle(request, response, new Object());

        // 증가 메서드는 호출되지 않음
        verify(redisTemplate, never()).opsForHash();
        verify(valueOps, never()).increment(anyString());
        // IP 키 TTL 갱신만 일어남
        verify(valueOps).set(IP_KEY, "1", Duration.ofMinutes(10));
    }

    // ===== 로그인 사용자 =====

    @Test
    @DisplayName("로그인 사용자 첫 방문: today/total 증가 + user/ip 키 둘 다 마킹")
    void loggedIn_firstVisit_increasesAndMarksBothKeys() {
        // given
        given(request.getRemoteAddr()).willReturn(IP);
        given(request.getHeader(KEY_OF_TOKEN_ID)).willReturn("jwt-xyz");
        given(tokenProvider.decodeToEmailByAccess("jwt-xyz")).willReturn("user@test.com");

        String userKey = Const.PREFIX_VISITOR_USER + "user@test.com";
        given(valueOps.get(userKey)).willReturn(null);
        org.mockito.Mockito.doReturn(hashOps).when(redisTemplate).opsForHash();

        // when
        interceptor.preHandle(request, response, new Object());

        // then
        verify(hashOps).increment(Const.KEY_TODAY_VISITOR, LocalDate.now().toString(), 1L);
        verify(valueOps).increment(Const.KEY_TOTAL_VISITOR);
        verify(valueOps).set(IP_KEY, "1", Duration.ofMinutes(10));
        verify(valueOps).set(userKey, "1", Duration.ofMinutes(10));
    }

    @Test
    @DisplayName("로그인 사용자 재방문(user 키 존재): 증가 없음, 두 키 모두 TTL 갱신")
    void loggedIn_repeatedVisit_doesNotIncrease() {
        given(request.getRemoteAddr()).willReturn(IP);
        given(request.getHeader(KEY_OF_TOKEN_ID)).willReturn("jwt-xyz");
        given(tokenProvider.decodeToEmailByAccess("jwt-xyz")).willReturn("user@test.com");

        String userKey = Const.PREFIX_VISITOR_USER + "user@test.com";
        given(valueOps.get(userKey)).willReturn("1"); // 이미 방문 기록 있음

        interceptor.preHandle(request, response, new Object());

        verify(redisTemplate, never()).opsForHash();
        verify(valueOps, never()).increment(anyString());
        verify(valueOps).set(IP_KEY, "1", Duration.ofMinutes(10));
        verify(valueOps).set(userKey, "1", Duration.ofMinutes(10));
    }

    // ===== IP 추출 우선순위 =====

    @Test
    @DisplayName("IP 추출: X-Forwarded-For 가 우선 사용된다")
    void ipExtraction_xForwardedForWins() {
        given(request.getHeader("X-Forwarded-For")).willReturn("198.51.100.7");
        given(request.getHeader(KEY_OF_TOKEN_ID)).willReturn(null);

        String forwardedIpKey = Const.PREFIX_VISITOR_IP + "198.51.100.7";
        given(valueOps.get(UNKNOWN_KEY)).willReturn(null);
        given(valueOps.get(forwardedIpKey)).willReturn(null);
        org.mockito.Mockito.doReturn(hashOps).when(redisTemplate).opsForHash();

        interceptor.preHandle(request, response, new Object());

        // X-Forwarded-For 의 IP 가 키로 사용됨
        verify(valueOps).set(forwardedIpKey, "1", Duration.ofMinutes(10));
    }

    @Test
    @DisplayName("IP 추출: 헤더가 'unknown' 이면 다음 후보로 넘어가 RemoteAddr 사용")
    void ipExtraction_unknownHeaderFallsThrough() {
        given(request.getHeader("X-Forwarded-For")).willReturn("unknown");
        given(request.getHeader(KEY_OF_TOKEN_ID)).willReturn(null);
        given(request.getRemoteAddr()).willReturn(IP);

        given(valueOps.get(UNKNOWN_KEY)).willReturn(null);
        given(valueOps.get(IP_KEY)).willReturn(null);
        org.mockito.Mockito.doReturn(hashOps).when(redisTemplate).opsForHash();

        interceptor.preHandle(request, response, new Object());

        // RemoteAddr 의 IP 가 키로 사용됨
        verify(valueOps).set(IP_KEY, "1", Duration.ofMinutes(10));
    }
}
