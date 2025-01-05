package com.SeeAndYouGo.SeeAndYouGo.filter;

import com.SeeAndYouGo.SeeAndYouGo.like.dto.LikeResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.menu.dto.MenuResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.TokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class CacheFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(CacheFilter.class);
    private final RedisTemplate<String, byte[]> byteRedisTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TokenProvider tokenProvider;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 이미지 업로드 요청일 경우 필터체인 진행만 함
        String contentType = request.getContentType();
        boolean isMultipart = contentType != null && contentType.startsWith("multipart/");
        if (isMultipart) {
            filterChain.doFilter(request, response);
            return;
        }

        RequestWrapper reqWrapper = new RequestWrapper(request);

        // 캐시된 데이터가 있음
        // [1] 캐시 확인 후 리턴
        Map<String, Object> body = new HashMap<>();
        String uri = request.getRequestURI();
        String method = request.getMethod();
        byte[] cachedData = searchCache(body, uri, method);
        if (cachedData != null && cachedData.length > 0) {
            responseAtCache(response, cachedData);
            return;
        }

        // 캐시된 데이터가 없음: 필터 체인 진행 후 응답을 캐싱
        filterChain.doFilter(reqWrapper, response);

        ResponseWrapper resWrapper = new ResponseWrapper(response);
        byte[] bodyData = resWrapper.getResponseData();
        cache(uri, method, bodyData);
        if (uri.contains("image")) {
            response.setContentType("image/png");
        } else {
            // 주의: 이미지가 아니라면 바이너리에 이렇게 content type 지정해야 문자열로 잘 인코딩 된다.
            response.setContentType(resWrapper.getContentType() + "; charset=UTF-8");
            response.setCharacterEncoding(resWrapper.getCharacterEncoding());
        }
        ServletOutputStream out = response.getOutputStream();
        out.write(bodyData);
        out.flush();
    }

    private void cache(String uri, String method, byte[] data) {
        String[] uriParts = uri.split("/");
        String key = "";
        String hash = "";

        // 이미지 캐싱
        if (uri.startsWith("/api/images/") && method.equals("GET")) {
            String imgName = uriParts[uriParts.length - 1];
            key = "review:image:" + imgName;
            byteRedisTemplate.opsForValue().set(key, data);
            return;
        }

        // 좋아요 캐싱
        if (uri.startsWith("/api/review/like")) {
            String likerEmail = tokenProvider.decodeToEmail(uriParts[uriParts.length - 1]);
            String reviewId = uriParts[uriParts.length - 2];
            key = "review:like:" + reviewId;
            hash = likerEmail;
        }

        // 메뉴 캐싱
        if (uri.contains("daily-menu")) {
            String restaurant = uriParts[3];
            key = "menu:daily:" + restaurant;
            hash = uriParts.length > 4 ? tokenProvider.decodeToEmail(uriParts[4]) : "unknown";
        }
        if (uri.startsWith("/api/weekly-menu/")) {  // admin 이 사용하는 /api/weekly-menu 는 캐싱 대상 아님
            key = "menu:weekly";
            hash = uriParts[3];
        }

        // (공통) 캐싱처리
        if (!key.isEmpty()) {
            if (hash.isEmpty())
                redisTemplate.opsForValue().set(key, new String(data));
            else
                redisTemplate.opsForHash().put(key, hash, new String(data));
        }
    }

    private byte[] searchCache(Map<String, Object> body, String uri, String method) throws IOException {
        String[] uriParts = uri.split("/");

        // 이미지 캐시 찾기
        if (uri.startsWith("/api/images/") && method.equals("GET")) { // review image
            String imgName = uriParts[uriParts.length - 1];
            String key = "review:image:" + imgName;
            // 찾기
            byte[] cachedData = byteRedisTemplate.opsForValue().get(key);
            if (cachedData != null && cachedData.length > 0) {
                logger.info("캐시에서 데이터 발견: " + key);
                return cachedData;
            }
        }

        // 좋아요 캐시 찾기
        if (uri.startsWith("/api/review/like")) {
            String reviewId = uriParts[uriParts.length - 2];
            String key = "review:like:" + reviewId;
            String hash = tokenProvider.decodeToEmail(uriParts[uriParts.length - 1]);

            String cachedData = (String) redisTemplate.opsForHash().get(key, hash);
            if (cachedData != null && !cachedData.isEmpty()) {
                LikeResponseDto dto = objectMapper.readValue(cachedData, LikeResponseDto.class);
                if (dto.isMine())
                    return cachedData.getBytes(StandardCharsets.UTF_8);
                dto.setLike(!dto.isLike());
                redisTemplate.opsForHash().put(key, hash, objectMapper.writeValueAsString(dto));
                return objectMapper.writeValueAsBytes(dto);
            }
        }

        // 메뉴 캐시 찾기
        if (uri.startsWith("/api/daily-menu")) {
            String restaurant = uriParts[3];
            String key = "menu:daily:" + restaurant;
            String hash = uriParts.length > 4 ? tokenProvider.decodeToEmail(uriParts[4]) : "unknown";

            String cachedData = (String) redisTemplate.opsForHash().get(key, hash);
            if (cachedData != null && !cachedData.isEmpty()) {
                LikeResponseDto dto = objectMapper.readValue(cachedData, LikeResponseDto.class);
                return objectMapper.writeValueAsBytes(dto);
            }
        }

        if (uri.startsWith("/api/weekly-menu/")) {
            String key = "menu:weekly";
            String hash = uriParts[3];

            String cachedData = (String) redisTemplate.opsForHash().get(key, hash);
            if (cachedData != null && !cachedData.isEmpty()) {
                MenuResponseDto dto = objectMapper.readValue(cachedData, MenuResponseDto.class);
                return objectMapper.writeValueAsBytes(dto);
            }
        }

        return new byte[0];
    }

    private void responseAtCache(HttpServletResponse response, byte[] cachedData) throws IOException {
        // 바이너리 응답 처리
        ServletOutputStream out = response.getOutputStream();
        out.write(cachedData);
        out.flush();
        logger.info("캐시된 바이너리 응답을 OutputStream에 전송했습니다.");
    }

    private Map<String, String> extractPathParameters(HttpServletRequest request, String pathPattern) {
        Map<String, String> pathParams = new HashMap<>();

        String requestUri = request.getRequestURI();
        String regexPattern = pathPattern.replaceAll("\\{([^/]+)\\}", "([^/]+)");
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(requestUri);

        List<String> paramNames = new ArrayList<>();
        Matcher nameMatcher = Pattern.compile("\\{([^/]+)\\}").matcher(pathPattern);
        while (nameMatcher.find()) {
            paramNames.add(nameMatcher.group(1));
        }

        if (matcher.matches()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                pathParams.put(paramNames.get(i - 1), matcher.group(i));
            }
        }

        return pathParams;
    }
}