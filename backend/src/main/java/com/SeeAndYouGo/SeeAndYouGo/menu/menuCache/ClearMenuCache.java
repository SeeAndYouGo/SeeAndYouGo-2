package com.SeeAndYouGo.SeeAndYouGo.menu.menuCache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메뉴 캐시를 무효화(삭제)하는 통합 어노테이션
 * daily-menu, weekly-menu 등 다양한 캐시 키 패턴을 지원
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClearMenuCache {
    
    /**
     * 캐시 키 패턴들을 지정 (예: {"daily-menu", "weekly-menu"})
     * 각 패턴에 대해 :{restaurantNumber} 형태로 키가 생성됨
     */
    String[] cacheKeys();
    
    /**
     * Restaurant 번호 파라미터의 이름을 지정 (String/Integer 타입용)
     * Restaurant 타입 파라미터는 이름에 상관없이 자동 인식됨
     * 기본값은 "restaurant"
     */
    String restaurantParam() default "restaurant";
    
    /**
     * 모든 레스토랑의 캐시를 삭제할지 여부
     * true인 경우 restaurantParam은 무시되고 모든 캐시가 삭제됨 (1~6)
     */
    boolean clearAll() default false;
}