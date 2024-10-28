package com.SeeAndYouGo.SeeAndYouGo.oAuth.config;

import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.JwtFilter;
import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// 직접 만든 tokenprovider와 jwtfilter를 securityconfig에 적용할 때 사용
@RequiredArgsConstructor
public class JwtSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private final TokenProvider tokenProvider;

    // tokenprovider를 주입받아서 jwtfilter를 통해 securityconfig 안에 필터를 등록.
    @Override
    public void configure(HttpSecurity http) {
        // security 로직에 JwtFilter 등록
        JwtFilter customFilter = new JwtFilter(tokenProvider);
        http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
    }
}