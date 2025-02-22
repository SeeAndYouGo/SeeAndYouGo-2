package com.SeeAndYouGo.SeeAndYouGo;

import com.SeeAndYouGo.SeeAndYouGo.connection.Connection;
import com.SeeAndYouGo.SeeAndYouGo.connection.ConnectionRepository;
import com.SeeAndYouGo.SeeAndYouGo.dish.Dish;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishRepository;
import com.SeeAndYouGo.SeeAndYouGo.dish.DishType;
import com.SeeAndYouGo.SeeAndYouGo.keyword.Keyword;
import com.SeeAndYouGo.SeeAndYouGo.keyword.KeywordRepository;
import com.SeeAndYouGo.SeeAndYouGo.menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuRepository;
import com.SeeAndYouGo.SeeAndYouGo.menu.MenuType;
import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.TokenProvider;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import com.SeeAndYouGo.SeeAndYouGo.review.Review;
import com.SeeAndYouGo.SeeAndYouGo.review.ReviewRepository;
import com.SeeAndYouGo.SeeAndYouGo.user.Social;
import com.SeeAndYouGo.SeeAndYouGo.user.User;
import com.SeeAndYouGo.SeeAndYouGo.user.UserRepository;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;

public class TestSetUp {

    public static Connection saveConnection(ConnectionRepository connectionRepository, int connected, String dateTime, Restaurant restaurant) {
        Connection connection = Connection.builder()
                                            .connected(connected)
                                            .time(dateTime)
                                            .restaurant(restaurant)
                                            .build();

        return connectionRepository.save(connection);
    }

    public static void stubDecodeToEmail(String email, TokenProvider tokenProvider, String jwtToken) {
        // jwtToken이 null이면 any로 선언
        if(jwtToken == null){
            Mockito.doReturn(email)
                    .when(tokenProvider)
                    .decodeToEmailByAccess(any(String.class));
        }else{
            Mockito.doReturn(email)
                    .when(tokenProvider)
                    .decodeToEmailByAccess(jwtToken);
        }
    }

    public static User saveUser(UserRepository userRepository, String email, String nickname, Social socialType) {
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .socialType(socialType)
                .build();

        return userRepository.save(user);
    }

    public static Dish saveDish(DishRepository dishRepository, String dishName, DishType dishType) {
        Dish dish = Dish.builder()
                .name(dishName)
                .dishType(dishType)
                .build();

        return dishRepository.save(dish);
    }

    public static Menu saveMenu(MenuRepository menuRepository, int price, LocalDate dateTime, Dept dept, MenuType menuType, Restaurant restaurant,
                                Dish... dishes) {
        Menu menu = Menu.builder()
                .price(price)
                .date(dateTime.toString())
                .dept(dept)
                .menuType(menuType)
                .restaurant(restaurant)
                .build();

        for (Dish dish : dishes) {
            menu.addDish(dish);
        }

        return menuRepository.save(menu);
    }

    public static Review saveReview(ReviewRepository reviewRepository, String email, String nickname,
                                    LocalDateTime dateTime, int likeCnt, Menu menu, String comment, String imgLink,
                                    double rate, Restaurant restaurant, int reportCnt) {
        Review review = Review.builder()
                .writerEmail(email)
                .writerNickname(nickname)
                .madeTime(dateTime.toString())
                .likeCount(likeCnt)
                .menu(menu)
                .comment(comment)
                .imgLink(imgLink)
                .reviewRate(rate)
                .restaurant(restaurant)
                .reportCount(reportCnt)
                .build();

        return reviewRepository.save(review);
    }

    public static Keyword saveKeyword(KeywordRepository keywordRepository, String keywordName) {
        Keyword keyword = Keyword.builder()
                                    .name(keywordName)
                                    .build();

        return keywordRepository.save(keyword);
    }
}
