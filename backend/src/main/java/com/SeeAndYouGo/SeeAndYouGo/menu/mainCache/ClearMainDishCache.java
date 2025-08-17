package com.SeeAndYouGo.SeeAndYouGo.menu.mainCache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메인 메뉴 캐시를 무효화(삭제)하는 어노테이션
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClearMainDishCache {
    
    /**
     * Restaurant 파라미터의 이름을 지정
     * 기본값은 "restaurant"
     */
    String restaurantParam() default "restaurant";
    
    /**
     * 모든 레스토랑의 캐시를 삭제할지 여부
     * true인 경우 restaurantParam은 무시되고 다 삭제됨
     */
    boolean clearAll() default false;
}