package com.SeeAndYouGo.SeeAndYouGo.review;

import com.SeeAndYouGo.SeeAndYouGo.menu.Menu;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByWriterEmailOrderByMadeTimeDesc(String userEmail);

    List<Review> findByRestaurantAndMenuIn(Restaurant restaurant, List<Menu> date);

    List<Review> findByRestaurantAndMenuInOrderByMadeTimeDesc(Restaurant restaurant, List<Menu> menus);

    List<Review> findByRestaurant(Restaurant restaurant);

    List<Review> findByRestaurantOrderByMadeTimeDesc(Restaurant restaurant);
}
