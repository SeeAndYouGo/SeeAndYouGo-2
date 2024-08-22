package com.SeeAndYouGo.SeeAndYouGo.Review;

import com.SeeAndYouGo.SeeAndYouGo.Menu.Dept;
import com.SeeAndYouGo.SeeAndYouGo.Restaurant.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
//    List<Review> findTop3ByRestaurantNameAndMadeTimeStartingWithOrderByMadeTimeDesc(@Param("restaurantName") String restaurantName, @Param("date") String date);
//
//    @Query("SELECT r FROM Review r " +
//            "WHERE r.restaurant.name = :restaurantName " +
//            "AND r.menu.dept = :dept " +
//            "AND FUNCTION('SUBSTRING', r.madeTime, 1, 10) = FUNCTION('SUBSTRING', :date, 1, 10) " +
//            "ORDER BY r.madeTime DESC")
//    List<Review> findTop3ByRestaurantDeptOrderByMadeTimeDesc(@Param("restaurantName") String restaurantName,
//                                                             @Param("date") String date,
//                                                             @Param("dept") Dept dept);
//
//    @Query("select r from Review r " +
//            "where FUNCTION('SUBSTRING', r.madeTime, 1, 10) = FUNCTION('SUBSTRING', :date, 1, 10) " +
//            "order by r.madeTime desc")
//    List<Review> findAllByMadeTime(@Param("date") String date);

//    @Query("select r from Review r " +
//            "where FUNCTION('SUBSTRING', r.madeTime, 1, 10) = FUNCTION('SUBSTRING', :date, 1, 10) " +
//            "and r.restaurant.name = :restaurantName " +
//            "order by r.madeTime desc")
//    List<Review> findReviewsByRestaurantAndDate(@Param("restaurantName") String restaurantName, @Param("date") String date);

    List<Review> findByWriterEmailOrderByMadeTimeDesc(String userEmail);

//    @Query(value = "SELECT * FROM review " +
//            "WHERE menu_id IN ( " +
//            "SELECT menu_id FROM menu_dish " +
//            "WHERE dish_id IN " +
//            "(SELECT DISTINCT md.dish_id FROM menu_dish md " +
//            "JOIN dish d ON md.dish_id = d.dish_id " +
//            "WHERE md.menu_id IN " +
//            "(SELECT menu_id FROM menu WHERE date= :date AND restaurant_id = :restaurantId) " +
//            "AND d.dish_type='MAIN') " +
//            ")", nativeQuery = true)
//    List<Review> findRestaurantReviews(@Param("restaurantId") Long restaurantId, @Param("date") String date);


    List<Review> findByRestaurantAndMadeTimeStartingWith(Restaurant restaurant, String date);
}
