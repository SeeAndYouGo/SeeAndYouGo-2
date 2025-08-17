package com.SeeAndYouGo.SeeAndYouGo.menu.menuCache;

import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;

/**
 * 메뉴 캐시 무효화를 위한 통합 AOP Aspect
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class MenuCacheAspect {

    private final MenuCacheService menuCacheService;

    /**
     * @ClearMenuCache 어노테이션이 있는 메서드 실행 후 캐시를 무효화
     */
    @AfterReturning("@annotation(clearCache)")
    public void clearMenuCache(JoinPoint joinPoint, ClearMenuCache clearCache) {
        try {
            String[] cacheKeys = clearCache.cacheKeys();
            
            if (clearCache.clearAll()) {
                // 모든 레스토랑의 캐시 무효화
                menuCacheService.clearAllMenuCache(cacheKeys);
                log.info("Cleared all menu cache for keys: {} after method: {}", 
                        String.join(", ", cacheKeys), joinPoint.getSignature().getName());
            } else {
                // 특정 레스토랑의 캐시 무효화
                String restaurantNumber = extractRestaurantParameter(joinPoint, clearCache.restaurantParam());
                if (restaurantNumber != null) {
                    menuCacheService.clearMenuCache(cacheKeys, restaurantNumber);
                    log.info("Cleared menu cache for keys: {} and restaurant: {} after method: {}", 
                            String.join(", ", cacheKeys), restaurantNumber, joinPoint.getSignature().getName());
                } else {
                    log.warn("Could not find restaurant parameter '{}' in method: {}", 
                            clearCache.restaurantParam(), joinPoint.getSignature().getName());
                }
            }
        } catch (Exception e) {
            log.error("Failed to clear menu cache after method: {}", 
                    joinPoint.getSignature().getName(), e);
        }
    }

    /**
     * 메서드 파라미터에서 Restaurant 번호를 추출
     */
    private String extractRestaurantParameter(JoinPoint joinPoint, String parameterName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            
            // Case 1) 파라미터 이름이 일치하고 타입이 String/Integer인 경우
            if (parameter.getName().equals(parameterName)) {
                Object arg = args[i];
                if (arg != null && isValidRestaurantNumber(arg.toString())) {
                    return arg.toString();
                }
            }
            
            // Case 2) Restaurant 타입인 경우 (파라미터 이름 상관없이)
            if (parameter.getType().equals(Restaurant.class)) {
                Restaurant restaurant = (Restaurant) args[i];
                if (restaurant != null) {
                    return String.valueOf(restaurant.getNumber());
                }
            }
        }

        return null;
    }

    /**
     * 유효한 레스토랑 번호인지 확인 (1~6)
     */
    private boolean isValidRestaurantNumber(String number) {
        try {
            int num = Integer.parseInt(number);
            return num >= 1 && num <= 6;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}