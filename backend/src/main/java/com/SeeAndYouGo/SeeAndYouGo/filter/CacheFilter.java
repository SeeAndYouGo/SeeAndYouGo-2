package com.SeeAndYouGo.SeeAndYouGo.filter;

import com.SeeAndYouGo.SeeAndYouGo.like.LikeService;
import com.SeeAndYouGo.SeeAndYouGo.like.dto.LikeResponseDto;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class CacheFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(CacheFilter.class);
    private final RedisTemplate<String, byte[]> byteRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TokenProvider tokenProvider;
    private final LikeService likeService;
    private final RedisTemplate redisTemplate;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CacheHttpServletRequestWrapper reqWrapper = new CacheHttpServletRequestWrapper(request);
        CacheHttpServletResponseWrapper resWrapper = new CacheHttpServletResponseWrapper(response);
        logger.info("CacheFilter activated for URI: " + request.getRequestURI());

        // [1] 요청 데이터
        String uri = reqWrapper.getRequestURI();
        String method = reqWrapper.getMethod();
        String contentType = reqWrapper.getContentType();
        // Map<String, Object> body = getJsonBody(reqWrapper);


        // 멀티파트 담아 리뷰 올릴 때 문제가 되기 때문에 여기가 존재 (왜 문제가 되나?)
        // 멀티파트 요청인지 확인
        // 멀티파트 요청은 래핑하지 않고 그대로 필터 체인 진행
        boolean isMultipart = contentType != null && contentType.startsWith("multipart/");
        if (isMultipart) {
            filterChain.doFilter(request, response);
            return;
        }
        

//        try {
        Map<String, Object> body = new HashMap<>();

        // [2] 캐시 확인 후 리턴
        byte[] cachedData = searchCache(body, uri, method);
        if (cachedData != null && cachedData.length > 0) {
            responseAtCache(response, cachedData, resWrapper);
            return;
        }

        // [3] 필터 체인 진행 - 래퍼된 응답 객체 사용
        filterChain.doFilter(reqWrapper, resWrapper);

        // [4] 응답
        byte[] bodyData = resWrapper.getResponseData();
        cache(uri, method, bodyData);

        // contents type 지정 후 응답하기
        if (uri.contains("image")) {
            response.setContentType("image/png");
        } else {
            response.setContentType(resWrapper.getContentType() + "; charset=UTF-8");  // ㅠ 이거 지정때문에 애먹었네.. 바이너리를 이렇게 전달하면 문자열로 잘 인코딩 된다.
//            response.setContentType(resWrapper.getContentType()); // 이렇게 말고..
            response.setCharacterEncoding(resWrapper.getCharacterEncoding());
        }
        response.setContentLength(bodyData.length);
        ServletOutputStream out = response.getOutputStream();
        out.write(bodyData);
        out.flush();
    }

    // TODO: URI 분기시키기
    // 캐시값이 변경되어야 하는 경우, 로직이 포함되어야 함
    private void cache(String uri, String method, byte[] data) {
        String key = "";
        String hash = "";

        // 이미지 캐싱
        if (uri.startsWith("/api/images/") && method.equals("GET")) {
            String[] uriParts = uri.split("/");
            String imgName = uriParts[uriParts.length - 1];
            key = "review:image:" + imgName;
        }

        // 좋아요 캐싱
        if (uri.startsWith("/api/review/like")) {
            String[] uriParts = uri.split("/");
            String likerEmail = tokenProvider.decodeToEmail(uriParts[uriParts.length - 1]);
            String reviewId = uriParts[uriParts.length - 2];
            key = "review:like:" + reviewId;
            hash = likerEmail;
        }

        // 공통
        if (hash.isEmpty())
            byteRedisTemplate.opsForValue().set(key, data);
        else
            byteRedisTemplate.opsForHash().put(key, hash, new String(data));


        logger.info("캐시에 저장되었습니다: " + key + " -- " + hash + " -- " + new String(data));
    }

    // TODO: URI 분기시키기
    private byte[] searchCache(Map<String, Object> body, String uri, String method) throws IOException {
        // 이미지 캐시
        if (uri.startsWith("/api/images/") && method.equals("GET")) { // review image
            String[] uriParts = uri.split("/");
            String imgName = uriParts[uriParts.length - 1];
            String key = "review:image:" + imgName;
            byte[] cachedData = byteRedisTemplate.opsForValue().get(key);
            if (cachedData != null && cachedData.length > 0) {
                logger.info("캐시에서 데이터 발견: " + key);
                return cachedData;
            }
        }

        if (uri.startsWith("/api/review/like")) {
            String[] uriParts = uri.split("/");
            String likerEmail = tokenProvider.decodeToEmail(uriParts[uriParts.length - 1]);
            String reviewId = uriParts[uriParts.length - 2];
            String key = "review:like:" + reviewId;
            String hash = likerEmail;
            byte[] cachedData = (byte[]) redisTemplate.opsForHash().get(key, hash);
            if (cachedData != null && cachedData.length > 0) {
                logger.info("캐시에서 데이터 발견: " + key);
                LikeResponseDto dto = objectMapper.readValue(cachedData, LikeResponseDto.class);
                if (dto.isMine())
                    return cachedData;
                dto.setLike(!dto.isLike());
                return objectMapper.writeValueAsBytes(dto);
            }
        }

        return new byte[0];
    }

    private Map<String, Object> getJsonBody(HttpServletRequest request) throws IOException {
        if (request.getInputStream().available() == 0) {
            return new HashMap<>();
        }
        return objectMapper.readValue(request.getInputStream(), Map.class);
    }

    private void responseAtCache(HttpServletResponse response, byte[] cachedData, CacheHttpServletResponseWrapper resWrapper) throws IOException {
        // 응답 헤더 설정
        response.setContentType(resWrapper.getContentType());
        response.setCharacterEncoding(resWrapper.getCharacterEncoding());
        response.setContentLength(cachedData.length);

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
