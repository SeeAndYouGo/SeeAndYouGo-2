package com.SeeAndYouGo.SeeAndYouGo.global;

/**
 * 메뉴 관련 상수를 정의하는 클래스.
 */
public final class MenuConstants {

    private MenuConstants() {
        // 유틸리티 클래스이므로 인스턴스화 방지
    }

    /**
     * 메뉴 정보가 없을 때 사용하는 기본 메뉴명
     */
    public static final String DEFAULT_DISH_NAME = "메뉴 정보 없음";

    /**
     * 운영 중단 상태를 나타내는 문자열
     */
    public static final String OPERATION_SUSPENDED = "운영중단";

    /**
     * 운영 안함 상태를 나타내는 문자열
     */
    public static final String NOT_OPERATING = "운영안함";
}
