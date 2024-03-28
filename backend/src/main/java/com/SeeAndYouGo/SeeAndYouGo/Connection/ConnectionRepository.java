package com.SeeAndYouGo.SeeAndYouGo.Connection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {
    @Query("SELECT c FROM Connection c " +
            "WHERE c.time = (SELECT MAX(c2.time) FROM Connection c2 " +
            "WHERE c2.restaurant.name = :name) " +
            "AND c.restaurant.name = :name")
    Connection findRecent(@Param("name") String name);

    @Query("SELECT MAX(ct.time) FROM Connection ct")
    String findRecentTime();

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Connection e")
    boolean existsAnyData();

    @Query(value = "SELECT c FROM Connection c " +
            "WHERE c.restaurant.name = :name " +
            "AND c.time LIKE :date%", nativeQuery = true)
    List<Connection> findByRestaurantNameAndDate(@Param("name") String restaurantName, @Param("date") String date);
}
