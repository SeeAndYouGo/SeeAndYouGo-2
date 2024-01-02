package com.SeeAndYouGo.SeeAndYouGo.OAuth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findBySocialId(String kakaoId);
}