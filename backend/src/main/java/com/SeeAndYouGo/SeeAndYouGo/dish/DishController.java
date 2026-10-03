package com.SeeAndYouGo.SeeAndYouGo.dish;

import com.SeeAndYouGo.SeeAndYouGo.caching.annotation.EvictAllCache;
import com.SeeAndYouGo.SeeAndYouGo.dish.dto.DishRequestDto;
import com.SeeAndYouGo.SeeAndYouGo.dish.dto.DishResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.user.AdminAuthorizationService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final AdminAuthorizationService adminAuthorizationService;

    @PutMapping("/main-menu")
    @EvictAllCache({"daily-menu", "weekly-menu"})
    public String updateMainDish(@RequestBody List<MainDishRequestDto> mainDishResponseDtos,
                                 @Parameter(hidden = true) @AuthenticationPrincipal String email){   // 받아오는 4개 중 mainMenuName만 사용할 것임
        adminAuthorizationService.assertAdmin(email);
        mainDishResponseDtos.removeAll(Collections.singletonList(null));
        dishService.updateMainDish(mainDishResponseDtos);
        return "Main Menu reflect Success.";
    }

    @GetMapping("/dish/week")
    public List<DishResponseDto> getWeeklyDish(@Parameter(hidden = true) @AuthenticationPrincipal String email){
        adminAuthorizationService.assertAdmin(email);
        // 해당 주(week)의 시작 날짜와 끝 날짜 계산
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        return dishService.getWeeklyDish(startOfWeek, endOfWeek);
    }

    @DeleteMapping("/dish/{id}")
    @EvictAllCache({"daily-menu", "weekly-menu"})
    public boolean dishDelete(@PathVariable Long id,
                              @Parameter(hidden = true) @AuthenticationPrincipal String email){
        adminAuthorizationService.assertAdmin(email);
        return dishService.deleteDish(id);
    }

    @PutMapping("/dish/name")
    @EvictAllCache({"daily-menu", "weekly-menu"})
    public boolean dishUpdateName(@RequestBody DishRequestDto dishRequestDto,
                                  @Parameter(hidden = true) @AuthenticationPrincipal String email) {
        adminAuthorizationService.assertAdmin(email);
        return dishService.updateDishName(dishRequestDto.getId(), dishRequestDto.getChangeName());
    }
}
