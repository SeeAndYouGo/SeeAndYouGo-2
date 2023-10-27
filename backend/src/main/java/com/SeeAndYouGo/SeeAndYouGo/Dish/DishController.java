package com.SeeAndYouGo.SeeAndYouGo.Dish;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DishController {
    private final DishService dishService;

    @GetMapping("/week")
    public void week() throws Exception {
        dishService.saveAndCashWeekDish(1);
        dishService.saveAndCashWeekDish(2);
        dishService.saveAndCashWeekDish(3);
    }

    @GetMapping("/day")
    public void day() throws Exception {
        dishService.saveAndCashTodayDish();
    }


}
