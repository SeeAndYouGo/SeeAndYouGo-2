package com.SeeAndYouGo.SeeAndYouGo.Dish;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.LinkedList;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class DishController {
    private final DishService dishService;

    @PutMapping("/main-menu")
    @Caching(evict = {
            @CacheEvict(value="getWeeklyMenu",  allEntries=true),
            @CacheEvict(value="getDailyMenu", allEntries=true)})
    public ResponseEntity updateMainDish(@RequestBody LinkedList<MainDishRequestDto> mainDishResponsDtos){   // 받아오는 4개 중 mainMenuName만 사용할 것임
        mainDishResponsDtos.removeAll(Collections.singletonList(null));
        dishService.updateMainDish(mainDishResponsDtos);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/week")
    @Caching(evict = {
            @CacheEvict(value="getWeeklyMenu",  allEntries=true),
            @CacheEvict(value="getDailyMenu", allEntries=true)})
    public void week() throws Exception {
        dishService.saveAndCacheWeekDish(1);
        dishService.saveAndCacheWeekDish(2);
        dishService.saveAndCacheWeekDish(3);
    }

    @PostMapping("/dish/test/{page}")
    public String bridgeDish(@PathVariable int page) throws Exception {
        return dishService.fetchDishInfoToString(page);
    }
}
