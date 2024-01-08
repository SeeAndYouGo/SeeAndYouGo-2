package com.SeeAndYouGo.SeeAndYouGo.Dish;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class DishController {
    private final DishService dishService;

    @PutMapping("/mainMenu")
    public String updateMainDish(@RequestBody LinkedList<MainDishResponse> mainDishResponses){
        mainDishResponses.removeAll(Collections.singletonList(null));
        dishService.updateMainDish(mainDishResponses);
        return "Data updated successfully";
    }

    @GetMapping("/week")
    public void week() throws Exception {
        dishService.saveAndCacheWeekDish(1);
        dishService.saveAndCacheWeekDish(2);
        dishService.saveAndCacheWeekDish(3);
    }
}
