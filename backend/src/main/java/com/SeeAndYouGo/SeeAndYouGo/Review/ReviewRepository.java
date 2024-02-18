package com.SeeAndYouGo.SeeAndYouGo.Review;

import com.SeeAndYouGo.SeeAndYouGo.Menu.Dept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findTop3ByRestaurantNameAndMadeTimeStartingWithOrderByMadeTimeDesc(@Param("restaurantName") String restaurantName, @Param("date") String date);

    @Query("SELECT r FROM Review r " +
            "WHERE r.restaurant.name = :restaurantName " +
            "AND r.menu.dept = :dept " +
            "AND FUNCTION('SUBSTRING', r.madeTime, 1, 10) = FUNCTION('SUBSTRING', :date, 1, 10) " +
            "ORDER BY r.madeTime DESC")
    List<Review> findTop3ByRestaurantDeptOrderByMadeTimeDesc(@Param("restaurantName") String restaurantName,
                                                             @Param("date") String date,
                                                             @Param("dept") Dept dept);

    @Query("select r from Review r " +
            "where FUNCTION('SUBSTRING', r.madeTime, 1, 10) = FUNCTION('SUBSTRING', :date, 1, 10) " +
            "order by r.madeTime desc")
    List<Review> findAllByMadeTime(@Param("date") String date);

    @Query("select r from Review r " +
            "where FUNCTION('SUBSTRING', r.madeTime, 1, 10) = FUNCTION('SUBSTRING', :date, 1, 10) " +
            "and r.restaurant.name = :restaurantName " +
            "order by r.madeTime desc")
    List<Review> findReviewsByRestaurantAndDate(@Param("restaurantName") String restaurantName, @Param("date") String date);

    List<Review> findByWriterEmailOrderByMadeTimeDesc(String userEmail);
}
