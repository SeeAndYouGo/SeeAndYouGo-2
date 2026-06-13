package com.SeeAndYouGo.SeeAndYouGo.menu.menuProvider;

/**
 * 메뉴 데이터를 가져오는 방식
 */
public enum MenuProviderType {
    JSON,       // JSON 파일에서 읽기 (고정 메뉴 식당)
    API,        // 외부 API 호출
    CRAWLING    // 웹 크롤링
}
