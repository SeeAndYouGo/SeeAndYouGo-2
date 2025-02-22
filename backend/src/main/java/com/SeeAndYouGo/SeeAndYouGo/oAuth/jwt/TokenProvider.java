package com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt;

import com.SeeAndYouGo.SeeAndYouGo.oAuth.TokenDto;
import com.SeeAndYouGo.SeeAndYouGo.oAuth.UserRole;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import com.SeeAndYouGo.SeeAndYouGo.user.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TokenProvider {
    private final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

    private static final String AUTHORITIES_KEY = "auth";
    private final UserRepository userRepository;

    private SecretKey secretKey;

    @Value("${jwt.expiration.access}")
    private long accessExpiration;

    @Value("${jwt.expiration.refresh}")
    private long refreshExpiration;

    @Value("${jwt.secret}")
    private String secret;

    public TokenProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    private void init() {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(this.secret));
    }

    public TokenDto createToken(String email) {

        UsernamePasswordAuthenticationToken userAuthentication = new UsernamePasswordAuthenticationToken(email,
                null,
                Collections.singleton(new SimpleGrantedAuthority(UserRole.USER.toString())));

        String accessToken = createAccessToken(userAuthentication);
        String refreshToken = createRefreshToken(userAuthentication);

        return TokenDto.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private String createRefreshToken(Authentication authentication) {

        String email = authentication.getName();

        String refreshToken = Jwts.builder()
                .setSubject(email) // set email
                .setExpiration(getExpireTime(refreshExpiration))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        User user = userRepository.findByEmail(email);
        user.updateRefreshToken(refreshToken);
        userRepository.save(user);
        return refreshToken;
    }

    public String createAccessToken(Authentication authentication) {
        String email = authentication.getName();

        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(email) // set email
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .setExpiration(getExpireTime(accessExpiration))
                .compact();
    }


    public TokenDto reIssueToken(Authentication authentication, String refreshToken) {
        String accessToken = createAccessToken(authentication);
        String newRefreshToken = refreshToken;
        if (isRefreshTokenExpiringSoon(refreshToken)) {
            newRefreshToken = createRefreshToken(authentication);
        }

        return new TokenDto(accessToken, newRefreshToken, "");
    }

    private static Date getExpireTime(long timeMillis) {
        long now = (new Date()).getTime();
        return new Date(now + timeMillis);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            logger.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            logger.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            logger.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            logger.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    public String decodeToEmailByAccess(String jwtToken) throws ArrayIndexOutOfBoundsException{
        // 사용자가 불분명할 때는 빈 string을 준다.
        if(jwtToken == null || jwtToken.equals("null")) return "";
        try {
            // JWT 디코드 및 검증
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(jwtToken)
                    .getBody();
            return (String) claims.get("sub");  // 이메일 추출
        } catch (SignatureException e) {
            return "";
        }
    }


    public boolean isRefreshTokenExpired(String refreshToken) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey) // 🔹 서명 검증을 위한 secretKey
                    .parseClaimsJws(refreshToken)
                    .getBody();

            Date expiration = claims.getExpiration(); // 🔹 만료 시간 가져오기
            return expiration.before(new Date()); // 🔹 현재 시간과 비교

        } catch (ExpiredJwtException e) {
            return true; // 🔹 만료됨
        } catch (SignatureException e) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
    }


    private boolean isRefreshTokenExpiringSoon(String refreshToken) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(refreshToken)
                .getBody();

        Date expiration = claims.getExpiration();
        Date now = new Date();

        // 현재 시간 + 3일(밀리초)
        long threeDaysMillis = 3 * 24 * 60 * 60 * 1000L;
        Date threeDaysLater = new Date(now.getTime() + threeDaysMillis);

        // 만료 시간이 3일 이내인지 체크
        return expiration.before(threeDaysLater);
    }
}