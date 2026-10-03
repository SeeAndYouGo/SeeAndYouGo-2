package com.SeeAndYouGo.SeeAndYouGo.admin;

import com.SeeAndYouGo.SeeAndYouGo.dish.DishController;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishService;
import com.SeeAndYouGo.SeeAndYouGo.dish.dto.DishResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuController;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuService;
import com.SeeAndYouGo.SeeAndYouGo.menu.dto.MenuResponseByAdminDto;
import com.SeeAndYouGo.SeeAndYouGo.user.AdminAuthorizationService;
import com.SeeAndYouGo.SeeAndYouGo.user.UserRepository;
import com.SeeAndYouGo.SeeAndYouGo.userKeyword.UserKeywordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminApiAuthorizationTest {

    @Mock
    private DishService dishService;

    @Mock
    private AdminAuthorizationService adminAuthorizationService;

    @Mock
    private MenuService menuService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserKeywordRepository userKeywordRepository;

    @Mock
    private com.SeeAndYouGo.SeeAndYouGo.dish.DishRepository dishRepository;

    @Test
    void dishWeek_requiresAdminAuthorizationService() {
        DishController controller = new DishController(dishService, adminAuthorizationService);
        List<DishResponseDto> expected = List.of(new DishResponseDto(1L, "비빔밥"));

        when(dishService.getWeeklyDish(any(LocalDate.class), any(LocalDate.class))).thenReturn(expected);

        List<DishResponseDto> result = controller.getWeeklyDish("admin@seeandyougo.com");

        verify(adminAuthorizationService).assertAdmin("admin@seeandyougo.com");
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void weeklyMenuForAdmin_requiresAdminAuthorizationService() {
        MenuController controller = new MenuController(
                menuService,
                adminAuthorizationService,
                userRepository,
                userKeywordRepository,
                dishRepository
        );

        when(menuService.getOneWeekRestaurantMenu(anyString(), anyString())).thenReturn(emptyWeekMenu());

        List<MenuResponseByAdminDto> result = controller.allRestaurantMenuWeekForAdmin("admin@seeandyougo.com");

        verify(adminAuthorizationService).assertAdmin("admin@seeandyougo.com");
        assertThat(result).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private List<Menu>[] emptyWeekMenu() {
        List<Menu>[] weekMenus = new List[7];
        for (int i = 0; i < weekMenus.length; i++) {
            weekMenus[i] = Collections.emptyList();
        }
        return weekMenus;
    }
}
