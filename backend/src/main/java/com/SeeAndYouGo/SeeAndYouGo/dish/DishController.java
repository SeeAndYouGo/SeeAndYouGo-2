package com.SeeAndYouGo.SeeAndYouGo.dish;

import com.SeeAndYouGo.SeeAndYouGo.global.response.ApiResponse;
import com.SeeAndYouGo.SeeAndYouGo.caching.annotation.EvictAllCache;
import com.SeeAndYouGo.SeeAndYouGo.dish.dto.DishRequestDto;
import com.SeeAndYouGo.SeeAndYouGo.dish.dto.DishResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.user.AuthorizationService;
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
@RequestMapping("/api/admin/dishes")
@CrossOrigin(origins = "http://localhost:3000")
public class DishController {
    private static final String MAIN_DISH_UPDATED_MESSAGE = "대표 메뉴가 반영되었습니다.";
    private static final String DISH_DELETED_MESSAGE = "메뉴가 삭제되었습니다.";
    private static final String DISH_NAME_UPDATED_MESSAGE = "메뉴명이 변경되었습니다.";
    private static final String DISH_NAME_UNCHANGED_MESSAGE = "변경된 내용이 없습니다.";

    private final DishService dishService;
    private final AuthorizationService authorizationService;

    @PutMapping("/main")
    @EvictAllCache({"daily-menu", "weekly-menu"})
    public ApiResponse<String> updateMainDish(@RequestBody List<MainDishRequestDto> mainDishResponseDtos,
                                              @Parameter(hidden = true) @AuthenticationPrincipal String email){   // 받아오는 4개 중 mainMenuName만 사용할 것임
        authorizationService.assertAdmin(email);
        mainDishResponseDtos.removeAll(Collections.singletonList(null));
        dishService.updateMainDish(mainDishResponseDtos);
        return ApiResponse.success("Main Menu reflect Success.", MAIN_DISH_UPDATED_MESSAGE);
    }

    @GetMapping("/weekly")
    public List<DishResponseDto> getWeeklyDish(@Parameter(hidden = true) @AuthenticationPrincipal String email){
        authorizationService.assertAdmin(email);
        // 해당 주(week)의 시작 날짜와 끝 날짜 계산
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        return dishService.getWeeklyDish(startOfWeek, endOfWeek);
    }

    @DeleteMapping("/{id}")
    @EvictAllCache({"daily-menu", "weekly-menu"})
    public ApiResponse<Boolean> dishDelete(@PathVariable Long id,
                                           @Parameter(hidden = true) @AuthenticationPrincipal String email){
        authorizationService.assertAdmin(email);
        dishService.deleteDish(id);
        return ApiResponse.success(true, DISH_DELETED_MESSAGE);
    }

    @PutMapping("/name")
    @EvictAllCache({"daily-menu", "weekly-menu"})
    public ApiResponse<Boolean> dishUpdateName(@RequestBody DishRequestDto dishRequestDto,
                                               @Parameter(hidden = true) @AuthenticationPrincipal String email) {
        authorizationService.assertAdmin(email);
        boolean updated = dishService.updateDishName(dishRequestDto.getId(), dishRequestDto.getChangeName());
        return ApiResponse.success(updated, updated ? DISH_NAME_UPDATED_MESSAGE : DISH_NAME_UNCHANGED_MESSAGE);
    }
}
