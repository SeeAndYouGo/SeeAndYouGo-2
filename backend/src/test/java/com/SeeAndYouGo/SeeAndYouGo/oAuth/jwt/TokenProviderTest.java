package com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt;

import com.SeeAndYouGo.SeeAndYouGo.oAuth.TokenDto;
import com.SeeAndYouGo.SeeAndYouGo.oAuth.UserRole;
import com.SeeAndYouGo.SeeAndYouGo.user.Social;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import com.SeeAndYouGo.SeeAndYouGo.user.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * 테스트 목표: JWT 발급/검증/디코딩/만료체크의 핵심 동작 검증.
 *
 *  - createToken: access/refresh 발급 + refresh 를 User 에 저장
 *  - validateToken: 정상/만료/위변조/형식이상 토큰 처리
 *  - decodeToEmailByAccess: 정상 → sub(이메일) / null/"null"/위변조 → ""
 *  - isRefreshTokenExpired: 정상 → false, 만료 → true, 위변조 → IllegalArgumentException
 *  - reIssueToken: 새 access/refresh + message="reissue"
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JWT - TokenProvider")
class TokenProviderTest {

    // HS512 는 64바이트 이상 key가 필요. 아래는 96바이트짜리 문자열을 base64로 인코딩한 값.
    private static final String BASE64_SECRET = Base64.getEncoder().encodeToString(
            "test-secret-key-for-junit-jupiter-token-provider-must-be-long-enough-for-hmac-sha512-x".getBytes()
    );
    private static final long ACCESS_EXP = 60_000L;
    private static final long REFRESH_EXP = 120_000L;
    private static final String EMAIL = "user@test.com";

    @Mock private UserRepository userRepository;

    private TokenProvider tokenProvider;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        tokenProvider = new TokenProvider(userRepository);
        ReflectionTestUtils.setField(tokenProvider, "secret", BASE64_SECRET);
        ReflectionTestUtils.setField(tokenProvider, "accessExpiration", ACCESS_EXP);
        ReflectionTestUtils.setField(tokenProvider, "refreshExpiration", REFRESH_EXP);
        ReflectionTestUtils.invokeMethod(tokenProvider, "init");
        secretKey = (SecretKey) ReflectionTestUtils.getField(tokenProvider, "secretKey");
    }

    // ===== createToken =====

    @Test
    @DisplayName("createToken: access/refresh 를 발급하고, refresh 는 User 에 저장된다")
    void createToken_issuesAndPersistsRefresh() {
        // given
        User user = User.builder().email(EMAIL).socialType(Social.KAKAO).build();
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(user));

        // when
        TokenDto dto = tokenProvider.createToken(EMAIL);

        // then
        assertThat(dto.getToken()).isNotBlank();
        assertThat(dto.getRefreshToken()).isNotBlank();
        assertThat(user.getRefreshToken()).isEqualTo(dto.getRefreshToken());
        verify(userRepository).save(user);

        Claims claims = parseClaims(dto.getToken());
        assertThat(claims.getSubject()).isEqualTo(EMAIL);
        assertThat(claims.get("auth")).isEqualTo(UserRole.USER.toString());
    }

    @Test
    @DisplayName("createToken: 존재하지 않는 사용자에 대해서는 IllegalArgumentException 을 던진다")
    void createToken_userNotFound() {
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tokenProvider.createToken(EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(EMAIL);
    }

    // ===== validateToken =====

    @Test
    @DisplayName("validateToken: 유효한 토큰은 true")
    void validateToken_valid() {
        String token = issueToken(EMAIL, ACCESS_EXP, secretKey);
        assertThat(tokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("validateToken: 만료된 토큰은 false")
    void validateToken_expired() {
        String expired = issueToken(EMAIL, -1000L, secretKey);
        assertThat(tokenProvider.validateToken(expired)).isFalse();
    }

    @Test
    @DisplayName("validateToken: 서명이 다른 토큰은 false")
    void validateToken_wrongSignature() {
        SecretKey other = Keys.hmacShaKeyFor(
                "another-secret-with-enough-length-to-pass-hmac-sha512-validation-requirement-yo".getBytes()
        );
        String wrongSigned = issueToken(EMAIL, ACCESS_EXP, other);
        assertThat(tokenProvider.validateToken(wrongSigned)).isFalse();
    }

    @Test
    @DisplayName("validateToken: 형식이 깨진 문자열은 false")
    void validateToken_malformed() {
        assertThat(tokenProvider.validateToken("not.a.valid.jwt")).isFalse();
    }

    // ===== decodeToEmailByAccess =====

    @Test
    @DisplayName("decodeToEmailByAccess: 정상 토큰은 sub(이메일)을 반환한다")
    void decodeToEmail_valid() {
        String token = issueToken(EMAIL, ACCESS_EXP, secretKey);
        assertThat(tokenProvider.decodeToEmailByAccess(token)).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("decodeToEmailByAccess: null 또는 \"null\" 문자열은 빈 문자열을 반환한다 (게스트 처리)")
    void decodeToEmail_nullInput() {
        assertThat(tokenProvider.decodeToEmailByAccess(null)).isEqualTo("");
        assertThat(tokenProvider.decodeToEmailByAccess("null")).isEqualTo("");
    }

    @Test
    @DisplayName("decodeToEmailByAccess: 서명이 다른 토큰은 빈 문자열을 반환한다")
    void decodeToEmail_wrongSignature() {
        SecretKey other = Keys.hmacShaKeyFor(
                "another-secret-with-enough-length-to-pass-hmac-sha512-validation-requirement-yo".getBytes()
        );
        String wrongSigned = issueToken(EMAIL, ACCESS_EXP, other);
        assertThat(tokenProvider.decodeToEmailByAccess(wrongSigned)).isEqualTo("");
    }

    // ===== isRefreshTokenExpired =====

    @Test
    @DisplayName("isRefreshTokenExpired: 유효한 토큰은 false")
    void refreshExpired_valid() {
        String token = issueToken(EMAIL, REFRESH_EXP, secretKey);
        assertThat(tokenProvider.isRefreshTokenExpired(token)).isFalse();
    }

    @Test
    @DisplayName("isRefreshTokenExpired: 만료된 토큰은 true")
    void refreshExpired_expired() {
        String expired = issueToken(EMAIL, -1000L, secretKey);
        assertThat(tokenProvider.isRefreshTokenExpired(expired)).isTrue();
    }

    @Test
    @DisplayName("isRefreshTokenExpired: 서명이 다른 토큰은 IllegalArgumentException")
    void refreshExpired_wrongSignature() {
        SecretKey other = Keys.hmacShaKeyFor(
                "another-secret-with-enough-length-to-pass-hmac-sha512-validation-requirement-yo".getBytes()
        );
        String wrongSigned = issueToken(EMAIL, REFRESH_EXP, other);
        assertThatThrownBy(() -> tokenProvider.isRefreshTokenExpired(wrongSigned))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ===== reIssueToken =====

    @Test
    @DisplayName("reIssueToken: 새 access/refresh 가 발급되고 message=\"reissue\" 가 설정된다")
    void reIssueToken_returnsNewPair() {
        // given
        User user = User.builder().email(EMAIL).socialType(Social.KAKAO).build();
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(user));

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                EMAIL, null,
                Collections.singleton(new SimpleGrantedAuthority(UserRole.USER.toString()))
        );

        // when
        TokenDto dto = tokenProvider.reIssueToken(auth, "old-refresh");

        // then
        assertThat(dto.getToken()).isNotBlank();
        assertThat(dto.getRefreshToken()).isNotBlank();
        assertThat(dto.getMessage()).isEqualTo("reissue");
        assertThat(user.getRefreshToken()).isEqualTo(dto.getRefreshToken());
    }

    // ===== helpers =====

    private static String issueToken(String email, long ttlMillis, SecretKey key) {
        return Jwts.builder()
                .setSubject(email)
                .claim("auth", UserRole.USER.toString())
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(new Date(System.currentTimeMillis() + ttlMillis))
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
