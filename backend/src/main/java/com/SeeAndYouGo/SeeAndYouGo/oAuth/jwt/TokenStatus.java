package com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt;

/**
 * 토큰 검증 결과.
 * 만료(EXPIRED)와 그 외 무효(INVALID)는 클라이언트 대응이 다르다.
 * 만료는 재발급 후 재시도, 무효는 즉시 로그아웃이다.
 */
public enum TokenStatus {
    VALID,
    EXPIRED,
    INVALID
}
