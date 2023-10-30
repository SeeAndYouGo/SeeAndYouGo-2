package com.SeeAndYouGo.SeeAndYouGo.Dish;

import com.SeeAndYouGo.SeeAndYouGo.Menu.Dept;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DishController {
    private final DishService dishService;

    @PutMapping("/mainMenu")
    public String updateMainDish(@RequestBody List<MainDishResponse> mainDishResponses){

        dishService.updateMainDish(mainDishResponses);
        return "Data updated successfully";
    }

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
