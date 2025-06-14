package com.SeeAndYouGo.SeeAndYouGo.visitor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VisitorCountRepository extends JpaRepository<VisitorCount, Long> {

    VisitorCount findByIsTotalFalseAndCreatedAt(LocalDate today);

    VisitorCount findTopByIsTotalTrueOrderByCreatedAtDesc();

    VisitorCount findByIsTotalTrueAndCreatedAt(LocalDate localDate);
}