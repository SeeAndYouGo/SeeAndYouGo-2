package com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt;

import com.SeeAndYouGo.SeeAndYouGo.oAuth.TokenDto;
import com.SeeAndYouGo.SeeAndYouGo.user.Social;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import com.SeeAndYouGo.SeeAndYouGo.user.UserRepository;
import com.SeeAndYouGo.SeeAndYouGo.user.UserType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenProviderTest {

    @Mock
    private UserRepository userRepository;

    private TokenProvider tokenProvider;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        tokenProvider = new TokenProvider(userRepository);

        String rawSecret = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        String encodedSecret = Base64.getEncoder().encodeToString(rawSecret.getBytes(StandardCharsets.UTF_8));

        ReflectionTestUtils.setField(tokenProvider, "secret", encodedSecret);
        ReflectionTestUtils.setField(tokenProvider, "accessExpiration", 3600000L);
        ReflectionTestUtils.setField(tokenProvider, "refreshExpiration", 7200000L);
        ReflectionTestUtils.invokeMethod(tokenProvider, "init");

        secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(encodedSecret));
    }

    @Test
    void createToken_includesAdminUserTypeInAccessAndRefreshTokens() {
        User adminUser = User.builder()
                .email("admin@seeandyougo.com")
                .nickname("admin")
                .socialType(Social.GOOGLE)
                .userType(UserType.ADMIN)
                .build();

        when(userRepository.findByEmail(adminUser.getEmail())).thenReturn(Optional.of(adminUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TokenDto tokenDto = tokenProvider.createToken(adminUser.getEmail());

        Claims accessClaims = parseClaims(tokenDto.getToken());
        Claims refreshClaims = parseClaims(tokenDto.getRefreshToken());

        assertThat(tokenDto.getUserType()).isEqualTo("ADMIN");
        assertThat(accessClaims.getSubject()).isEqualTo(adminUser.getEmail());
        assertThat(accessClaims.get("auth", String.class)).isEqualTo("ADMIN");
        assertThat(accessClaims.get("userType", String.class)).isEqualTo("ADMIN");
        assertThat(refreshClaims.get("auth", String.class)).isEqualTo("ADMIN");
        assertThat(refreshClaims.get("userType", String.class)).isEqualTo("ADMIN");
    }

    @Test
    void reIssueToken_usesCurrentUserTypeFromDatabase() {
        User adminUser = User.builder()
                .email("manager@seeandyougo.com")
                .nickname("manager")
                .socialType(Social.KAKAO)
                .userType(UserType.ADMIN)
                .build();

        when(userRepository.findByEmail(adminUser.getEmail())).thenReturn(Optional.of(adminUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsernamePasswordAuthenticationToken staleUserAuthentication =
                new UsernamePasswordAuthenticationToken(
                        adminUser.getEmail(),
                        null,
                        Collections.singleton(new SimpleGrantedAuthority("USER"))
                );

        TokenDto tokenDto = tokenProvider.reIssueToken(staleUserAuthentication, "legacy-refresh-token");
        Claims accessClaims = parseClaims(tokenDto.getToken());

        assertThat(tokenDto.getUserType()).isEqualTo("ADMIN");
        assertThat(accessClaims.get("auth", String.class)).isEqualTo("ADMIN");
        assertThat(accessClaims.get("userType", String.class)).isEqualTo("ADMIN");
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
