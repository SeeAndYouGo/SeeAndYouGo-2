package com.SeeAndYouGo.SeeAndYouGo.dish;

import com.SeeAndYouGo.SeeAndYouGo.dish.dto.DishRequestDto;
import com.SeeAndYouGo.SeeAndYouGo.dish.dto.DishResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class DishController {
    private final DishService dishService;

    @PutMapping("/main-menu")
    public String updateMainDish(@RequestBody List<MainDishRequestDto> mainDishRequestDtos){   // 받아오는 4개 중 mainMenuName만 사용할 것임
        log.info("Request to update main dishes with {} items.", mainDishRequestDtos.size());
        mainDishRequestDtos.removeAll(Collections.singletonList(null));
        dishService.updateMainDish(mainDishRequestDtos);
        log.info("Successfully updated main dishes.");
        return "Main Menu reflect Success.";
    }

    @GetMapping("/dish/week")
    public List<DishResponseDto> getWeeklyDish(){
        // 해당 주(week)의 시작 날짜와 끝 날짜 계산
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        return dishService.getWeeklyDish(startOfWeek, endOfWeek);
    }

    @DeleteMapping("/dish/{id}")
    public boolean dishDelete(@PathVariable Long id){
        log.info("Request to delete dish with ID: {}", id);
        boolean result = dishService.deleteDish(id);
        log.info("Dish with ID: {} deletion result: {}", id, result);
        return result;
    }

    @PutMapping("/dish/name")
    public boolean dishUpdateName(@RequestBody DishRequestDto dishRequestDto) {
        log.info("Request to update dish name for ID: {}. New name: {}", dishRequestDto.getId(), dishRequestDto.getChangeName());
        boolean result = dishService.updateDishName(dishRequestDto.getId(), dishRequestDto.getChangeName());
        log.info("Dish name update result for ID: {}: {}", dishRequestDto.getId(), result);
        return result;
    }
}