package com.SeeAndYouGo.SeeAndYouGo.caching;

import lombok.Builder;
import lombok.Data;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Data
@Builder
public class CacheContext {
    /**
     * HTTP 요청 객체
     */
    private HttpServletRequest request;

    /**
     * 요청 URL
     */
    private String url;

    /**
     * HTTP 메서드
     */
    private String method;

    /**
     * URL 파라미터 (경로 변수)
     */
    private Map<String, String> pathVariables;

    /**
     * 쿼리 파라미터
     */
    private Map<String, String> queryParams;

    /**
     * 요청 헤더
     */
    private Map<String, String> headers;

    /**
     * 사용자 인증 정보 (있는 경우)
     */
    private String userEmail;

    /**
     * 요청에 사용자를 식별할 수 있는 토큰이 실려 있는지 확인
     */
    public boolean isLoggedIn() {
        return userEmail != null && !userEmail.isEmpty();
    }
}