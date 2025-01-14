package com.SeeAndYouGo.SeeAndYouGo.visitor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface VisitorCountRepository extends JpaRepository<VisitorCount, Long> {

    @Query("SELECT v FROM VisitorCount v WHERE v.isTotal = false AND DATE(v.createdAt) = CURRENT_DATE")
    Optional<VisitorCount> findTodayTempData();

    void deleteByIsTotalFalse();

    Optional<VisitorCount> findByIsTotalTrue();
}