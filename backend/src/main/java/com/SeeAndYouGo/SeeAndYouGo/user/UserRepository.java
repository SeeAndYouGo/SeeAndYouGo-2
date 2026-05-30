package com.SeeAndYouGo.SeeAndYouGo.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Long countByNickname(String nickname);

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}