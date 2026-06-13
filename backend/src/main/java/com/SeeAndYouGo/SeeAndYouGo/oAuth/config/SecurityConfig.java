package com.SeeAndYouGo.SeeAndYouGo.oAuth.config;

import com.SeeAndYouGo.SeeAndYouGo.oAuth.UserRole;
import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.JwtAccessDeniedHandler;
import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.JwtAuthenticationEntryPoint;
import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;


    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .httpBasic().disable()
                .csrf().disable()     // 일단 프론트에서 token을 localstorage에 저장할 것이라 가정 후 csrf disable
                // 세션 없이 토큰을 주고받기에 세션 설정을 STATELESS
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // except 핸들링
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
                .and()
                .authorizeHttpRequests()
                .antMatchers(
                        HttpMethod.GET,
                        "/api/dish/week",
                        "/api/weekly-menu"
                ).hasAuthority(UserRole.ADMIN.name())
                .antMatchers(
                        HttpMethod.PUT,
                        "/api/dish/name",
                        "/api/main-menu"
                ).hasAuthority(UserRole.ADMIN.name())
                .antMatchers(HttpMethod.DELETE, "/api/dish/*")
                .hasAuthority(UserRole.ADMIN.name())
                .antMatchers("/", "/api*", "/api-docs/**", "/swagger-ui/**").permitAll()
                .anyRequest().permitAll()
                .and()
                .apply(new JwtSecurityConfig(tokenProvider));
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            final AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
