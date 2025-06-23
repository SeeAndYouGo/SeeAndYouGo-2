package com.SeeAndYouGo.SeeAndYouGo.caching;


import com.SeeAndYouGo.SeeAndYouGo.caching.CacheConfig.EmailExtractor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class CacheFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(CacheFilter.class);

    private final CacheService cacheService;
    private final List<CacheRule> cacheRules;
    private final EmailExtractor emailExtractor;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 이미지 업로드 요청 등 특정 요청은 캐싱 제외
        String contentType = request.getContentType();
        if (contentType != null && contentType.startsWith("multipart/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 요청 URL 및 메서드
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 적용 가능한 캐시 규칙 찾기
        Optional<CacheRule> matchingRule = findMatchingRule(uri, method);

        // 매칭되는 규칙이 없으면 그냥 필터 체인 진행
        if (!matchingRule.isPresent()) {
            filterChain.doFilter(request, response);
            return;
        }

        CacheRule rule = matchingRule.get();

        // 캐시 컨텍스트 생성
        CacheContext context = buildCacheContext(request, rule);

        // 캐시 키 생성
        String cacheKey = rule.generateKey(context);

        // 캐시에서 데이터 조회 - 캐시 타입에 따라 적절한 메소드 호출
        Optional<?> cachedData;
        if (rule.getCacheType() == CacheType.BYTES) {
            cachedData = cacheService.getBytesCache(rule.getCacheName(), cacheKey);
        } else {
            cachedData = cacheService.getStringCache(rule.getCacheName(), cacheKey);
        }

        // 캐시 히트: 캐시된 데이터로 응답
        if (cachedData.isPresent()) {
            if (rule.getCacheType() == CacheType.BYTES) {
                writeCachedResponse(response, (byte[]) cachedData.get(), uri);
            } else {
                writeCachedStringResponse(response, (String) cachedData.get(), uri);
            }
            return;
        }

        // 캐시 미스: 필터 체인 진행 후 응답 캐싱
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        // 요청 처리
        filterChain.doFilter(request, responseWrapper);

        // 응답 데이터 캐싱
        byte[] responseData = responseWrapper.getContentAsByteArray();
        // 응답 데이터 캐싱 부분의 코드 변경
        if (responseData.length > 0 && isSuccessResponse(responseWrapper)) {
            // 응답 유형에 따라 바이트 또는 문자열로 캐싱
            if (rule.getCacheType() == CacheType.BYTES) {
                if (rule.getTtl() != null) {
                    cacheService.putBytes(rule.getCacheName(), cacheKey, responseData, rule);
                } else {
                    cacheService.putBytes(rule.getCacheName(), cacheKey, responseData);
                }
            } else {
                String content = new String(responseData, StandardCharsets.UTF_8);
                if (rule.getTtl() != null) {
                    cacheService.putString(rule.getCacheName(), cacheKey, content, rule);
                } else {
                    cacheService.putString(rule.getCacheName(), cacheKey, content);
                }
            }
        }

        // 클라이언트에 응답 전송
        writeResponse(responseWrapper, responseData);
    }

    /**
     * 요청 URL과 메서드에 매칭되는 캐시 규칙 찾기
     */
    private Optional<CacheRule> findMatchingRule(String uri, String method) {
        return cacheRules.stream()
                .filter(rule -> rule.matches(uri, method))
                .findFirst();
    }

    /**
     * 캐시 컨텍스트 구성
     */
    private CacheContext buildCacheContext(HttpServletRequest request, CacheRule rule) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 경로 변수 추출
        Map<String, String> pathVariables = URLPatternMatcher.extractPathVariables(rule.getUrlPattern(), uri);

        // 쿼리 파라미터 추출
        Map<String, String> queryParams = extractQueryParams(request);

        // 헤더 추출
        Map<String, String> headers = extractHeaders(request);

        // 사용자 이메일 추출 (인증 헤더가 있는 경우)
        String authorization = request.getHeader("Authorization");
        String userEmail = emailExtractor.extractEmail(authorization);

        return CacheContext.builder()
                .request(request)
                .url(uri)
                .method(method)
                .pathVariables(pathVariables)
                .queryParams(queryParams)
                .headers(headers)
                .userEmail(userEmail)
                .build();
    }

    /**
     * 쿼리 파라미터 추출
     */
    private Map<String, String> extractQueryParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values.length > 0) {
                params.put(key, values[0]);
            }
        });
        return params;
    }

    /**
     * 헤더 추출
     */
    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        request.getHeaderNames().asIterator().forEachRemaining(name ->
                headers.put(name, request.getHeader(name))
        );
        return headers;
    }

    /**
     * 캐시된 바이트 데이터로 응답 작성
     */
    private void writeCachedResponse(HttpServletResponse response, byte[] data, String uri) throws IOException {
        // 이미지 응답인 경우 content type 설정
        if (uri.contains("/images/")) {
            response.setContentType("image/png");
        } else {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
        }

        ServletOutputStream out = response.getOutputStream();
        out.write(data);
        out.flush();
        logger.debug("응답이 바이트 캐시에서 제공되었습니다: {}", uri);
    }

    /**
     * 캐시된 문자열 데이터로 응답 작성
     */
    private void writeCachedStringResponse(HttpServletResponse response, String data, String uri) throws IOException {
        // 응답 유형에 따라 content type 설정
        if (uri.contains("/images/")) {
            response.setContentType("image/png");
        } else {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
        }

        response.getWriter().write(data);
        response.getWriter().flush();
        logger.debug("응답이 문자열 캐시에서 제공되었습니다: {}", uri);
    }

    /**
     * 응답 작성
     */
    private void writeResponse(ContentCachingResponseWrapper wrapper, byte[] data) throws IOException {
        wrapper.copyBodyToResponse();
        logger.debug("응답이 서비스에서 생성되고 캐시되었습니다: {}", wrapper.getContentType());
    }

    /**
     * 성공 응답인지 확인 (2xx 상태 코드)
     */
    private boolean isSuccessResponse(ContentCachingResponseWrapper response) {
        int status = response.getStatus();
        return status >= 200 && status < 300;
    }
}