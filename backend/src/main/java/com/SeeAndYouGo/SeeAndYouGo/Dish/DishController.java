package com.SeeAndYouGo.SeeAndYouGo.Dish;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.LinkedList;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class DishController {
    private final DishService dishService;

    @PutMapping("/main-menu")
    public void updateMainDish(@RequestBody LinkedList<MainDishRequestDto> mainDishResponsDtos){   // 받아오는 4개 중 mainMenuName만 사용할 것임
        mainDishResponsDtos.removeAll(Collections.singletonList(null));
        dishService.updateMainDish(mainDishResponsDtos);
    }

    @GetMapping("/week")
    public void week() throws Exception {
        dishService.saveAndCacheWeekDish(1);
        dishService.saveAndCacheWeekDish(2);
        dishService.saveAndCacheWeekDish(3);
    }

    @PostMapping("/dish/test")
    public String bridgeDish(@RequestParam int page, @RequestParam String AUTH_KEY, HttpServletResponse response) throws Exception {
        boolean isRightSecretKey = dishService.checkSecretKey(AUTH_KEY);

        if(!isRightSecretKey){
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return "Invalid AUTH_KEY: Unauthorized access";
        }

        return dishService.fetchDishInfoToString(page);
    }
}
