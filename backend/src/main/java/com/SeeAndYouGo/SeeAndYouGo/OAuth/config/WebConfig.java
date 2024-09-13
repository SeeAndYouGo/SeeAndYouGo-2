package com.SeeAndYouGo.SeeAndYouGo.OAuth.config;

import com.SeeAndYouGo.SeeAndYouGo.OAuth.jwt.TokenProvider;
import com.SeeAndYouGo.SeeAndYouGo.visitor.VisitorInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RedisTemplate<String, String> redisTemplate;
    private final TokenProvider tokenProvider;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
                .addMapping("/**") // 프로그램에서 제공하는 URL
                .allowedOriginPatterns("*") // 청을 허용할 출처를 명시, 전체 허용 (가능하다면 목록을 작성한다.
                .allowCredentials(true); // 쿠키 요청을 허용한다(다른 도메인 서버에 인증하는 경우에만 사용해야하며, true 설정시 보안상 이슈가 발생할 수 있다)
        // .maxAge(1500) // preflight 요청에 대한 응답을 브라우저에서 캐싱하는 시간 ;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new VisitorInterceptor(redisTemplate, tokenProvider))
                .addPathPatterns("/**");
    }
}