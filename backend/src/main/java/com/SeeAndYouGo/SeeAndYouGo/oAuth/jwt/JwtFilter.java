package com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt;

import com.SeeAndYouGo.SeeAndYouGo.global.exception.ErrorCode;
import com.SeeAndYouGo.SeeAndYouGo.global.response.ApiResponseWriter;
import com.SeeAndYouGo.SeeAndYouGo.oAuth.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String REFRESH_HEADER = "RefreshToken";
    public static final String BEARER_PREFIX = "Bearer ";
    private final TokenProvider tokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = request.getHeader(AUTHORIZATION_HEADER);
        String refreshToken = request.getHeader(REFRESH_HEADER);

        // Access token
        if (StringUtils.hasText(accessToken) && accessToken.startsWith(BEARER_PREFIX)) {
            String jwtToken = accessToken.substring(BEARER_PREFIX.length());
            TokenStatus status = tokenProvider.resolveTokenStatus(jwtToken);
            if (status == TokenStatus.VALID) {
                setAuthentication(tokenProvider.getAuthentication(jwtToken));
            } else {
                ApiResponseWriter.write(response, objectMapper, toErrorCode(status));
                return;
            }
        }

        // Refresh Token
        else if (StringUtils.hasText(refreshToken)) {
            TokenStatus status = tokenProvider.resolveTokenStatus(refreshToken);
            if (status == TokenStatus.VALID) {
                setAuthentication(tokenProvider.getAuthentication(refreshToken));
            } else {
                ApiResponseWriter.write(response, objectMapper, toErrorCode(status));
                return;
            }
        }
        
        // For guest
        else {
            setAuthenticationFromEmail("none", UserRole.GUEST);
        }

        // doFilter
        filterChain.doFilter(request, response);
    }

    // 만료는 재발급 후 재시도(AUTH_001), 그 외 무효는 즉시 로그아웃(AUTH_003)으로 안내한다.
    private ErrorCode toErrorCode(TokenStatus status) {
        return status == TokenStatus.EXPIRED ? ErrorCode.UNAUTHORIZED : ErrorCode.INVALID_TOKEN;
    }

    private void setAuthentication(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void setAuthenticationFromEmail(String email, UserRole role) {
        setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                email,
                null,
                java.util.Collections.singleton(new org.springframework.security.core.authority.SimpleGrantedAuthority(role.toString()))
        ));
    }
}
