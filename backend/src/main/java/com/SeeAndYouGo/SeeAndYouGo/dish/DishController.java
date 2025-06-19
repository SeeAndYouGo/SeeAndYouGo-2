package com.SeeAndYouGo.SeeAndYouGo.dish;

import com.SeeAndYouGo.SeeAndYouGo.dish.dto.DishResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.dish.dto.DuplicateDishResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class DishController {
    private final DishService dishService;

    @PutMapping("/main-menu")
    public String updateMainDish(@RequestBody List<MainDishRequestDto> mainDishResponseDtos){   // 받아오는 4개 중 mainMenuName만 사용할 것임
        mainDishResponseDtos.removeAll(Collections.singletonList(null));
        dishService.updateMainDish(mainDishResponseDtos);
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

    @GetMapping("/dish/duplicate")
    public DuplicateDishResponseDto dishDuplicate(@RequestParam String name){
        boolean result = dishService.duplicateDishName(name);

        return new DuplicateDishResponseDto(result);
    }

    @DeleteMapping("/dish/{id}")
    public boolean dishDelete(@PathVariable Long id){
        return dishService.deleteDish(id);
    }
}