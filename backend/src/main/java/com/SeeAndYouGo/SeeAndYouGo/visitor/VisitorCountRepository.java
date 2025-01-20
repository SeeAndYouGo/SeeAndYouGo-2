package com.SeeAndYouGo.SeeAndYouGo.visitor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VisitorCountRepository extends JpaRepository<VisitorCount, Long> {

    void deleteByIsTotalFalse();

    @Query(value = "SELECT * FROM visitor_count v WHERE v.is_total = false AND DATE(v.created_at) = CURRENT_DATE ORDER BY v.created_at DESC LIMIT 1", nativeQuery = true)
    Optional<VisitorCount> findRecentTodayBackupNative();

    @Query(value = "SELECT * FROM visitor_count v WHERE v.is_total = true AND DATE(v.created_at) = CURRENT_DATE ORDER BY v.created_at DESC LIMIT 1", nativeQuery = true)
    Optional<VisitorCount> findRecentTotalBackupNative();

}