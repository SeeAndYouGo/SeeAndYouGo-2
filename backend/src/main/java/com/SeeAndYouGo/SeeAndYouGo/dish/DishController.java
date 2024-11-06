package com.SeeAndYouGo.SeeAndYouGo.dish;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
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
}