package com.SeeAndYouGo.SeeAndYouGo.visitor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VisitorCountRepository extends JpaRepository<VisitorCount, Long> {

    void deleteByIsTotalFalse();

    @Query("SELECT v FROM VisitorCount v WHERE v.isTotal = false AND DATE(v.createdAt) = CURRENT_DATE")
    Optional<VisitorCount> findRecentTodayBackup();

    @Query("SELECT v FROM VisitorCount v WHERE v.isTotal = true AND DATE(v.createdAt) = CURRENT_DATE")
    Optional<VisitorCount> findRecentTotalBackup();
}