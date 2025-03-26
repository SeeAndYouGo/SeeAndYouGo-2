package com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt;

import com.SeeAndYouGo.SeeAndYouGo.oAuth.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String REFRESH_HEADER = "RefreshToken";
    public static final String BEARER_PREFIX = "Bearer ";
    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = request.getHeader(AUTHORIZATION_HEADER);
        String refreshToken = request.getHeader(REFRESH_HEADER);

        // Access token
        if (StringUtils.hasText(accessToken) && accessToken.startsWith(BEARER_PREFIX)) {
            String jwtToken = accessToken.substring(BEARER_PREFIX.length());
            if (tokenProvider.validateToken(jwtToken)) {
                String email = tokenProvider.decodeToEmailByAccess(jwtToken);
                setAuthenticationFromEmail(email, UserRole.USER);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                return;
            }
        }

        // Refresh Token
        else if (StringUtils.hasText(refreshToken)) {
            if (tokenProvider.validateToken(refreshToken)) {
                String email = tokenProvider.decodeToEmailByAccess(refreshToken);
                setAuthenticationFromEmail(email, UserRole.USER);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired refresh token");
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

    private void setAuthenticationFromEmail(String email, UserRole role) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email,
                null,
                Collections.singleton(new SimpleGrantedAuthority(role.toString())));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}