package com.SeeAndYouGo.SeeAndYouGo.visitor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VisitorCountRepository extends JpaRepository<VisitorCount, Long> {
    Optional<VisitorCount> findTopByOrderByCreatedAtDesc();
}