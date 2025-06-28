package com.SeeAndYouGo.SeeAndYouGo.caching;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.HashMap;
import java.util.Map;

public class URLPatternMatcher {

    private static final PathMatcher pathMatcher = new AntPathMatcher();

    /**
     * URL이 패턴과 일치하는지 확인
     */
    public static boolean matches(String url, String pattern) {
        return pathMatcher.match(pattern, url);
    }

    /**
     * URL에서 경로 변수 추출
     * 예: /api/menu/{restaurant} 패턴에서 /api/menu/student1 URL의 경우,
     * "restaurant" -> "student1" 맵을 반환
     */
    public static Map<String, String> extractPathVariables(String pattern, String url) {
        Map<String, String> variables = new HashMap<>();

        if (pathMatcher.match(pattern, url)) {
            variables.putAll(pathMatcher.extractUriTemplateVariables(pattern, url));
        }

        return variables;
    }
}
