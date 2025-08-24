package com.SeeAndYouGo.SeeAndYouGo.menu.mainCache;

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
 * 메인 메뉴 캐시 무효화를 위한 AOP Aspect
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class MainDishCacheAspect {

    private final NewDishCacheService newDishCacheService;

    /**
     * @ClearMainDishCache 어노테이션이 있는 메서드 실행 후 캐시를 무효화
     */
    @AfterReturning("@annotation(clearCache)")
    public void clearMainDishCache(JoinPoint joinPoint, ClearMainDishCache clearCache) {
        try {
            if (clearCache.clearAll()) {
                // 모든 레스토랑의 캐시 무효화
                newDishCacheService.clearAllHistoricalCache();
                log.info("Cleared all main dish cache after method: {}", joinPoint.getSignature().getName());
            } else {
                // 특정 레스토랑의 캐시 무효화
                Restaurant restaurant = extractRestaurantParameter(joinPoint, clearCache.restaurantParam());
                if (restaurant != null) {
                    newDishCacheService.clearHistoricalCache(restaurant);
                    log.info("Cleared main dish cache for restaurant: {} after method: {}", 
                            restaurant, joinPoint.getSignature().getName());
                } else {
                    log.warn("Could not find restaurant parameter '{}' in method: {}", 
                            clearCache.restaurantParam(), joinPoint.getSignature().getName());
                }
            }
        } catch (Exception e) {
            log.error("Failed to clear main dish cache after method: {}", 
                    joinPoint.getSignature().getName(), e);
        }
    }

    /**
     * 메서드 파라미터에서 Restaurant 객체를 추출
     */
    private Restaurant extractRestaurantParameter(JoinPoint joinPoint, String parameterName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            
            // 파라미터 이름이 일치하고 타입이 Restaurant인 경우
            if (parameter.getName().equals(parameterName) && 
                parameter.getType().equals(Restaurant.class)) {
                return (Restaurant) args[i];
            }
            
            // 파라미터 이름이 일치하지 않지만 타입이 Restaurant인 경우 (fallback)
            if (parameter.getType().equals(Restaurant.class)) {
                return (Restaurant) args[i];
            }
        }

        return null;
    }
}