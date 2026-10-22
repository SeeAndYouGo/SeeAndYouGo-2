package com.SeeAndYouGo.SeeAndYouGo.dish;

import com.SeeAndYouGo.SeeAndYouGo.global.exception.ApiException;
import com.SeeAndYouGo.SeeAndYouGo.global.exception.ErrorCode;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuService;
import com.SeeAndYouGo.SeeAndYouGo.menuDish.MenuDishRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("메뉴 삭제 - DishService.deleteDish")
class DishDeleteTest {

    @Mock private DishRepository dishRepository;
    @Mock private MenuService menuService;
    @Mock private MenuDishRepository menuDishRepository;

    @InjectMocks private DishService dishService;

    @Test
    @DisplayName("존재하는 메뉴는 메뉴-요리 연결을 지운 뒤 삭제된다")
    void deletesExistingDish() {
        Dish dish = mock(Dish.class);
        given(dishRepository.findById(1L)).willReturn(Optional.of(dish));

        dishService.deleteDish(1L);

        InOrder inOrder = inOrder(menuDishRepository, dishRepository);
        inOrder.verify(menuDishRepository).deleteByDishId(1L);
        inOrder.verify(dishRepository).delete(dish);
    }

    @Test
    @DisplayName("없는 메뉴면 DISH_001 예외가 나고 아무것도 지우지 않는다")
    void throwsDishNotFoundWhenMissing() {
        given(dishRepository.findById(999L)).willReturn(Optional.empty());

        ApiException e = catchThrowableOfType(() -> dishService.deleteDish(999L), ApiException.class);

        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.DISH_NOT_FOUND);
        assertThat(e.getMessage()).isEqualTo("ID 999에 해당하는 메뉴를 찾을 수 없습니다.");
        verify(menuDishRepository, never()).deleteByDishId(anyLong());
        verify(dishRepository, never()).delete(any());
    }
}
