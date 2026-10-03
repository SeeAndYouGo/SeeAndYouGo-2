package com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt;

import com.SeeAndYouGo.SeeAndYouGo.oAuth.TokenDto;
import com.SeeAndYouGo.SeeAndYouGo.oAuth.UserRole;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import com.SeeAndYouGo.SeeAndYouGo.user.UserRepository;
import com.SeeAndYouGo.SeeAndYouGo.user.UserType;
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
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TokenProvider {
    private final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

    private static final String AUTHORITIES_KEY = "auth";
    private static final String USER_TYPE_KEY = "userType";
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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
        Authentication userAuthentication = createAuthentication(email, user.getUserType());

        String accessToken = createAccessToken(userAuthentication);
        String refreshToken = createRefreshToken(userAuthentication);

        return TokenDto.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .userType(user.getUserType().name())
                .build();
    }

    private String createRefreshToken(Authentication authentication) {
        String email = authentication.getName();
        String authorities = getAuthorities(authentication);
        UserRole userRole = extractUserRole(authentication);

        String refreshToken = Jwts.builder()
                .setSubject(email) // set email
                .claim(AUTHORITIES_KEY, authorities)
                .claim(USER_TYPE_KEY, userRole.name())
                .setExpiration(getExpireTime(refreshExpiration))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
        user.updateRefreshToken(refreshToken);
        userRepository.save(user);
        return refreshToken;
    }

    public String createAccessToken(Authentication authentication) {
        String email = authentication.getName();
        String authorities = getAuthorities(authentication);
        UserRole userRole = extractUserRole(authentication);

        return Jwts.builder()
                .setSubject(email) // set email
                .claim(AUTHORITIES_KEY, authorities)
                .claim(USER_TYPE_KEY, userRole.name())
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .setExpiration(getExpireTime(accessExpiration))
                .compact();
    }

    public TokenDto reIssueToken(Authentication authentication, String refreshToken) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + authentication.getName()));
        Authentication currentAuthentication = createAuthentication(user.getEmail(), user.getUserType());
        String accessToken = createAccessToken(currentAuthentication);
        String newRefreshToken = createRefreshToken(currentAuthentication);

        return TokenDto.builder()
                .token(accessToken)
                .refreshToken(newRefreshToken)
                .userType(user.getUserType().name())
                .message("reissue")
                .build();
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
            return parseClaims(jwtToken).getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return "";
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        UserRole userRole = resolveUserRole(claims);
        return createAuthentication(claims.getSubject(), userRole);
    }

    public boolean isRefreshTokenExpired(String refreshToken) {
        try {
            Claims claims = parseClaims(refreshToken);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());

        } catch (ExpiredJwtException e) {
            return true; // 🔹 만료됨
        } catch (SignatureException e) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
    }

    private Authentication createAuthentication(String email, UserType userType) {
        UserRole userRole = userType == UserType.ADMIN ? UserRole.ADMIN : UserRole.USER;
        return createAuthentication(email, userRole);
    }

    private Authentication createAuthentication(String email, UserRole userRole) {
        return new UsernamePasswordAuthenticationToken(
                email,
                null,
                Collections.singleton(new SimpleGrantedAuthority(userRole.name()))
        );
    }

    private String getAuthorities(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }

    private UserRole extractUserRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(StringUtils::hasText)
                .map(UserRole::valueOf)
                .findFirst()
                .orElse(UserRole.USER);
    }

    private UserRole resolveUserRole(Claims claims) {
        String userType = claims.get(USER_TYPE_KEY, String.class);
        if (StringUtils.hasText(userType)) {
            return UserRole.valueOf(userType);
        }

        String authorities = claims.get(AUTHORITIES_KEY, String.class);
        if (!StringUtils.hasText(authorities)) {
            return UserRole.USER;
        }

        List<String> roles = Arrays.stream(authorities.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());

        return roles.isEmpty() ? UserRole.USER : UserRole.valueOf(roles.get(0));
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
