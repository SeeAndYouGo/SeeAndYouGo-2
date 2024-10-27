package com.SeeAndYouGo.SeeAndYouGo.menu;

import com.SeeAndYouGo.SeeAndYouGo.TestSetUp;
import com.SeeAndYouGo.SeeAndYouGo.dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishRepository;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishType;
import com.SeeAndYouGo.SeeAndYouGo.keyword.Keyword;
import com.SeeAndYouGo.SeeAndYouGo.keyword.KeywordRepository;
import com.SeeAndYouGo.SeeAndYouGo.menu.dto.MenuResponseByUserDto;
import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.TokenProvider;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.user.Social;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import com.SeeAndYouGo.SeeAndYouGo.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@SpringBootTest
@Transactional
public class MenuControllerTest {

    @Autowired
    private MenuController menuController;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KeywordRepository keywordRepository;

    @MockBean
    private TokenProvider tokenProvider;

    private final String userEmail = "test@daum.net";
    private final String userToken = "test";
    private final String userNickname = "test";
    private final Social userSocial = Social.KAKAO;

    private final String mainDishName = "김치찌개";
    private final String sideDishName = "현미밥";

    private final Integer menuPrice = 4000;
    private final LocalDate menuDate = LocalDate.now();
    private final Dept menuDept = Dept.STUDENT;
    private final MenuType menuMenuType = MenuType.BREAKFAST;
    private final Restaurant menuRestaurant = Restaurant.제2학생회관;

    @BeforeEach
    void init(){
        // decodeToEmail 수동 설정.
        TestSetUp.stubDecodeToEmail(userEmail, tokenProvider, userToken);

        // user 저장
        TestSetUp.saveUser(userRepository, userEmail, userNickname, userSocial);

        // dish 저장
        Dish 김치찌개 = TestSetUp.saveDish(dishRepository, mainDishName, DishType.MAIN);
        Dish 현미밥 = TestSetUp.saveDish(dishRepository, sideDishName, DishType.SIDE);

        // menu 저장
        TestSetUp.saveMenu(menuRepository, menuPrice, menuDate, menuDept, menuMenuType, menuRestaurant, 김치찌개, 현미밥);

        // keyword 저장
        TestSetUp.saveKeyword(keywordRepository, mainDishName);
        TestSetUp.saveKeyword(keywordRepository, sideDishName);
    }

    @DisplayName("비유저 금일 식단 메뉴 조회")
    @Test
    void 비유저_금일_식단_조회() throws Exception {
        // when
        List<MenuResponseByUserDto> menuResponseDtos = menuController.restaurantMenuDayByUser(menuRestaurant.toString(), null); // token이 null이면 로그인하지 않은 유저
        // 메뉴는 1개뿐이므로 .get(0)을 진행
        MenuResponseByUserDto menuResponseDto = menuResponseDtos.get(0);

        // then
        assertEquals(menuResponseDto.getRestaurantName(), menuRestaurant.toString());

        List<String> mainDishList = menuResponseDto.getMainDishList();
        assertEquals(mainDishList.size(), 1);

        String mainDish = mainDishList.get(0);
        assertEquals(mainDish, mainDishName);

        List<String> sideDishList = menuResponseDto.getSideDishList();
        assertEquals(sideDishList.size(), 1);

        String sideDish = sideDishList.get(0);
        assertEquals(sideDish, sideDishName);

        assertEquals(menuResponseDto.getPrice(), menuPrice);
        assertEquals(menuResponseDto.getDept(), menuDept.toString());
        assertEquals(menuResponseDto.getDate(), menuDate.toString());
        assertEquals(menuResponseDto.getMenuType(), menuMenuType.toString());

        List<String> keywordList = menuResponseDto.getKeywordList();
        assertEquals(keywordList.size(), 0);
    }

    @DisplayName("유저 금일 식단 메뉴 조회(키워드 등록 X)")
    @Test
    void 유저_금일_식단_조회_키워드X() throws Exception {
        // when
        List<MenuResponseByUserDto> menuResponseDtos = menuController.restaurantMenuDayByUser(menuRestaurant.toString(), userToken);
        // 메뉴는 1개뿐이므로 .get(0)을 진행
        MenuResponseByUserDto menuResponseDto = menuResponseDtos.get(0);

        // then
        assertEquals(menuResponseDto.getRestaurantName(), menuRestaurant.toString());

        List<String> mainDishList = menuResponseDto.getMainDishList();
        assertEquals(mainDishList.size(), 1);

        String mainDish = mainDishList.get(0);
        assertEquals(mainDish, mainDishName);

        List<String> sideDishList = menuResponseDto.getSideDishList();
        assertEquals(sideDishList.size(), 1);

        String sideDish = sideDishList.get(0);
        assertEquals(sideDish, sideDishName);

        assertEquals(menuResponseDto.getPrice(), menuPrice);
        assertEquals(menuResponseDto.getDept(), menuDept.toString());
        assertEquals(menuResponseDto.getDate(), menuDate.toString());
        assertEquals(menuResponseDto.getMenuType(), menuMenuType.toString());

        List<String> keywordList = menuResponseDto.getKeywordList();
        assertEquals(keywordList.size(), 0);
    }

    @DisplayName("유저 금일 식단 메뉴 조회(MainDish 키워드 등록 O)")
    @Test
    void 유저_금일_식단_조회_메인_키워드O() throws Exception {
        // given
        // 유저가 금일 식단 중 mainDish를 키워드로 등록한다.
        User user = userRepository.findByEmail(userEmail);
        Keyword keyword = keywordRepository.findByName(mainDishName);

        user.addKeyword(keyword);

        // when
        List<MenuResponseByUserDto> menuResponseDtos = menuController.restaurantMenuDayByUser(menuRestaurant.toString(), userToken);
        // 메뉴는 1개뿐이므로 .get(0)을 진행
        MenuResponseByUserDto menuResponseDto = menuResponseDtos.get(0);

        // then
        assertEquals(menuResponseDto.getRestaurantName(), menuRestaurant.toString());

        List<String> mainDishList = menuResponseDto.getMainDishList();
        assertEquals(mainDishList.size(), 1);

        String mainDish = mainDishList.get(0);
        assertEquals(mainDish, mainDishName);

        List<String> sideDishList = menuResponseDto.getSideDishList();
        assertEquals(sideDishList.size(), 1);

        String sideDish = sideDishList.get(0);
        assertEquals(sideDish, sideDishName);

        assertEquals(menuResponseDto.getPrice(), menuPrice);
        assertEquals(menuResponseDto.getDept(), menuDept.toString());
        assertEquals(menuResponseDto.getDate(), menuDate.toString());
        assertEquals(menuResponseDto.getMenuType(), menuMenuType.toString());

        List<String> keywordList = menuResponseDto.getKeywordList();
        assertEquals(keywordList.size(), 1);
        // 유저가 등록한 키워드가 들어가있어야 한다.
        assertEquals(keywordList.get(0), keyword.getName());
    }

    @DisplayName("유저 금일 식단 메뉴 조회(sideDish 키워드 등록 O)")
    @Test
    void 유저_금일_식단_조회_사이드_키워드O() throws Exception {
        // given
        // 유저가 금일 식단 중 mainDish를 키워드로 등록한다.
        User user = userRepository.findByEmail(userEmail);
        Keyword keyword = keywordRepository.findByName(sideDishName);

        user.addKeyword(keyword);

        // when
        List<MenuResponseByUserDto> menuResponseDtos = menuController.restaurantMenuDayByUser(menuRestaurant.toString(), userToken);
        // 메뉴는 1개뿐이므로 .get(0)을 진행
        MenuResponseByUserDto menuResponseDto = menuResponseDtos.get(0);

        // then
        assertEquals(menuResponseDto.getRestaurantName(), menuRestaurant.toString());

        List<String> mainDishList = menuResponseDto.getMainDishList();
        assertEquals(mainDishList.size(), 1);

        String mainDish = mainDishList.get(0);
        assertEquals(mainDish, mainDishName);

        List<String> sideDishList = menuResponseDto.getSideDishList();
        assertEquals(sideDishList.size(), 1);

        String sideDish = sideDishList.get(0);
        assertEquals(sideDish, sideDishName);

        assertEquals(menuResponseDto.getPrice(), menuPrice);
        assertEquals(menuResponseDto.getDept(), menuDept.toString());
        assertEquals(menuResponseDto.getDate(), menuDate.toString());
        assertEquals(menuResponseDto.getMenuType(), menuMenuType.toString());

        List<String> keywordList = menuResponseDto.getKeywordList();
        assertEquals(keywordList.size(), 1);
        // 유저가 등록한 키워드가 들어가있어야 한다.
        assertEquals(keywordList.get(0), keyword.getName());
    }

    @DisplayName("유저 금일 식단 메뉴 조회(main&sideDish 키워드 등록 O)")
    @Test
    void 유저_금일_식단_조회_메인_사이드_키워드O() throws Exception {
        // given
        // 유저가 금일 식단 중 mainDish와 sideDish를 키워드로 등록한다.
        User user = userRepository.findByEmail(userEmail);
        Keyword mainDishKeyword = keywordRepository.findByName(sideDishName);
        Keyword sideDishKeyword = keywordRepository.findByName(mainDishName);

        user.addKeyword(mainDishKeyword);
        user.addKeyword(sideDishKeyword);

        // when
        List<MenuResponseByUserDto> menuResponseDtos = menuController.restaurantMenuDayByUser(menuRestaurant.toString(), userToken);
        // 메뉴는 1개뿐이므로 .get(0)을 진행
        MenuResponseByUserDto menuResponseDto = menuResponseDtos.get(0);

        // then
        assertEquals(menuResponseDto.getRestaurantName(), menuRestaurant.toString());

        List<String> mainDishList = menuResponseDto.getMainDishList();
        assertEquals(mainDishList.size(), 1);

        String mainDish = mainDishList.get(0);
        assertEquals(mainDish, mainDishName);

        List<String> sideDishList = menuResponseDto.getSideDishList();
        assertEquals(sideDishList.size(), 1);

        String sideDish = sideDishList.get(0);
        assertEquals(sideDish, sideDishName);

        assertEquals(menuResponseDto.getPrice(), menuPrice);
        assertEquals(menuResponseDto.getDept(), menuDept.toString());
        assertEquals(menuResponseDto.getDate(), menuDate.toString());
        assertEquals(menuResponseDto.getMenuType(), menuMenuType.toString());

        List<String> keywordList = menuResponseDto.getKeywordList();
        assertEquals(keywordList.size(), 2);
        // 유저가 등록한 키워드가 들어가있어야 한다.

        // mainDish 중 유저가 등록한 키워드가 담긴 리스트
        List<String> mainDishKeywordList = keywordList.stream().filter(keywordName -> keywordName.equals(mainDishName)).collect(Collectors.toList());
        List<String> sideDishKeywordList = keywordList.stream().filter(keywordName -> keywordName.equals(sideDishName)).collect(Collectors.toList());

        assertEquals(mainDishKeywordList.size(), 1);
        assertEquals(mainDishKeywordList.get(0), mainDishName);
        assertEquals(sideDishKeywordList.size(), 1);
        assertEquals(sideDishKeywordList.get(0), sideDishName);
    }

    @DisplayName("식당별 주간메뉴 조회")
    @Test
    void 식당별_주간메뉴_조회() throws Exception {

    }


}
