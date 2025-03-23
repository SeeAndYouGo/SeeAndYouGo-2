package com.SeeAndYouGo.SeeAndYouGo.visitor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VisitorCountRepository extends JpaRepository<VisitorCount, Long> {

    void deleteByIsTotalTrueAndIdNot(Long id);

    Optional<VisitorCount> findTopByIsTotalTrueOrderByCountDesc();

    Optional<VisitorCount> findTopByIsTotalFalseOrderByCountDesc();

    Optional<VisitorCount> findByIsTotalTrueAndCreatedAtBefore(LocalDateTime time);
}