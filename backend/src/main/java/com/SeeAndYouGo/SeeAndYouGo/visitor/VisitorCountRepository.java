package com.SeeAndYouGo.SeeAndYouGo.visitor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VisitorCountRepository extends JpaRepository<VisitorCount, Long> {

    Optional<VisitorCount> findTopByIsTotalTrueOrderByCountDesc();

    Optional<VisitorCount> findTopByIsTotalFalseOrderByCountDesc();

    List<VisitorCount> findByIsTotalTrueAndCreatedAtBefore(LocalDateTime time);

    @Modifying
    void deleteByIsTotalTrueAndIdNotIn(List<Long> excludeIds);
}