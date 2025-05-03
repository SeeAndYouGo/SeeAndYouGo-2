package com.SeeAndYouGo.SeeAndYouGo.caching;

import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.http.HttpServletResponse;

/**
 * ContentCachingResponseWrapper를 확장해 필요한 추가 기능 제공
 */
public class ResponseWrapper extends ContentCachingResponseWrapper {

    public ResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    /**
     * 응답 데이터 가져오기
     */
    public byte[] getResponseData() {
        return getContentAsByteArray();
    }
}
