package com.SeeAndYouGo.SeeAndYouGo.menu.cache;

import com.SeeAndYouGo.SeeAndYouGo.dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuRepository;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewDishCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MenuRepository menuRepository;

    private static final String HISTORICAL_MAIN_DISHES_KEY = "historical:main-dishes:";
    private static final String HISTORICAL_LAST_SYNC_KEY = "historical:last-sync:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 어제 날짜의 메인메뉴들을 historical 캐시에 추가
     */
    public void syncHistoricalDishes(Restaurant restaurant) {
        String yesterday = LocalDate.now().minusDays(1).format(DATE_FORMATTER);
        String lastSyncDate = getLastSyncDate(restaurant);
        
        // 마지막 동기화 날짜부터 어제까지의 메뉴들을 캐시에 추가
        LocalDate startDate = lastSyncDate != null ? 
                LocalDate.parse(lastSyncDate, DATE_FORMATTER).plusDays(1) : 
                getNewDishCriteriaStartDate();
        LocalDate endDate = LocalDate.parse(yesterday, DATE_FORMATTER);
        
        if (startDate.isAfter(endDate)) {
            log.debug("No new dishes to sync for restaurant: {}", restaurant);
            return;
        }
        
        // 기간 동안의 메뉴들 조회
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<Menu> dayMenus = menuRepository.findByRestaurantAndDate(restaurant, date.format(DATE_FORMATTER));
            Set<String> mainDishNames = dayMenus.stream()
                    .flatMap(menu -> menu.getMainDish().stream())
                    .map(Dish::getName)
                    .collect(Collectors.toSet());
            
            if (!mainDishNames.isEmpty()) {
                String cacheKey = HISTORICAL_MAIN_DISHES_KEY + restaurant.toString();
                redisTemplate.opsForSet().add(cacheKey, mainDishNames.toArray());
            }
        }
        
        // 마지막 동기화 날짜 업데이트
        setLastSyncDate(restaurant, yesterday);
        log.info("Synced historical dishes for restaurant: {} until {}", restaurant, yesterday);
    }

    /**
     * 특정 레스토랑의 historical 캐시 무효화
     */
    public void clearHistoricalCache(Restaurant restaurant) {
        String dishCacheKey = HISTORICAL_MAIN_DISHES_KEY + restaurant.toString();
        String syncCacheKey = HISTORICAL_LAST_SYNC_KEY + restaurant.toString();
        
        redisTemplate.delete(dishCacheKey);
        redisTemplate.delete(syncCacheKey);
        
        log.info("Cleared historical cache for restaurant: {}", restaurant);
    }

    /**
     * 모든 레스토랑의 historical 캐시 무효화
     */
    public void clearAllHistoricalCache() {
        for (Restaurant restaurant : Restaurant.values()) {
            clearHistoricalCache(restaurant);
        }
        log.info("Cleared all historical caches");
    }

    /**
     * 레스토랑의 과거 메인메뉴들 조회 (캐시 우선, 없으면 DB에서 구성)
     */
    public Set<String> getHistoricalMainDishes(Restaurant restaurant) {
        String cacheKey = HISTORICAL_MAIN_DISHES_KEY + restaurant.toString();
        Set<Object> cachedDishes = redisTemplate.opsForSet().members(cacheKey);
        
        if (cachedDishes != null && !cachedDishes.isEmpty()) {
            return cachedDishes.stream()
                    .map(Object::toString)
                    .collect(Collectors.toSet());
        }
        
        // 캐시 미스 시 DB에서 구성
        return buildHistoricalCacheFromDB(restaurant);
    }

    /**
     * DB에서 historical 캐시 구성
     */
    private Set<String> buildHistoricalCacheFromDB(Restaurant restaurant) {
        LocalDate startDate = getNewDishCriteriaStartDate();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        
        List<Menu> historicalMenus = menuRepository.findByRestaurantAndDateGreaterThanEqual(
                restaurant, startDate.format(DATE_FORMATTER))
                .stream()
                .filter(menu -> LocalDate.parse(menu.getDate(), DATE_FORMATTER).isBefore(LocalDate.now()))
                .collect(Collectors.toList());
        
        Set<String> mainDishNames = historicalMenus.stream()
                .flatMap(menu -> menu.getMainDish().stream())
                .map(Dish::getName)
                .collect(Collectors.toSet());
        
        // 캐시에 저장
        if (!mainDishNames.isEmpty()) {
            String cacheKey = HISTORICAL_MAIN_DISHES_KEY + restaurant.toString();
            redisTemplate.opsForSet().add(cacheKey, mainDishNames.toArray());
            setLastSyncDate(restaurant, yesterday.format(DATE_FORMATTER));
        }
        
        log.info("Built historical cache from DB for restaurant: {}", restaurant);
        return mainDishNames;
    }

    /**
     * 신메뉴 기준 시작 날짜 (현재는 올해 1월 1일, 추후 설정으로 변경 가능)
     */
    private LocalDate getNewDishCriteriaStartDate() {
        return LocalDate.now().withDayOfYear(1);
    }

    /**
     * 마지막 동기화 날짜 조회
     */
    private String getLastSyncDate(Restaurant restaurant) {
        String cacheKey = HISTORICAL_LAST_SYNC_KEY + restaurant.toString();
        Object lastSync = redisTemplate.opsForValue().get(cacheKey);
        return lastSync != null ? lastSync.toString() : null;
    }

    /**
     * 마지막 동기화 날짜 설정
     */
    private void setLastSyncDate(Restaurant restaurant, String date) {
        String cacheKey = HISTORICAL_LAST_SYNC_KEY + restaurant.toString();
        redisTemplate.opsForValue().set(cacheKey, date);
    }

    /**
     * 서버 부팅 시 historical 캐시 동기화
     */
    public void initHistoricalCache() {
        log.info("Starting historical cache initialization");
        
        for (Restaurant restaurant : Restaurant.values()) {
            try {
                if (needsCacheSync(restaurant)) {
                    log.info("Syncing historical cache for restaurant: {}", restaurant);
                    syncHistoricalDishes(restaurant);
                } else {
                    log.debug("Historical cache is up to date for restaurant: {}", restaurant);
                }
            } catch (Exception e) {
                log.error("Failed to initialize historical cache for restaurant: {}", restaurant, e);
            }
        }
        
        log.info("Completed historical cache initialization");
    }

    /**
     * 캐시 동기화가 필요한지 검증
     */
    private boolean needsCacheSync(Restaurant restaurant) {
        String dishCacheKey = HISTORICAL_MAIN_DISHES_KEY + restaurant.toString();
        String syncCacheKey = HISTORICAL_LAST_SYNC_KEY + restaurant.toString();
        
        // 1. 캐시가 존재하지 않는 경우
        if (!redisTemplate.hasKey(dishCacheKey)) {
            log.debug("Cache does not exist for restaurant: {}", restaurant);
            return true;
        }
        
        // 2. 캐시가 비어있는 경우
        Long cacheSize = redisTemplate.opsForSet().size(dishCacheKey);
        if (cacheSize == null || cacheSize == 0) {
            log.debug("Cache is empty for restaurant: {}", restaurant);
            return true;
        }
        
        // 3. 마지막 동기화 날짜가 어제가 아닌 경우
        Object lastSync = redisTemplate.opsForValue().get(syncCacheKey);
        if (lastSync == null) {
            log.debug("No sync date found for restaurant: {}", restaurant);
            return true;
        }
        
        String yesterday = LocalDate.now().minusDays(1).format(DATE_FORMATTER);
        if (!yesterday.equals(lastSync.toString())) {
            log.debug("Cache is outdated for restaurant: {}. Last sync: {}, Expected: {}", 
                    restaurant, lastSync, yesterday);
            return true;
        }
        
        return false;
    }
}