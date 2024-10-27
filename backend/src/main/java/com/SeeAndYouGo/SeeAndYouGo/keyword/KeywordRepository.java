package com.SeeAndYouGo.SeeAndYouGo.keyword;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    Keyword findByName(String name);

    boolean existsByName(String keywordName);
}
