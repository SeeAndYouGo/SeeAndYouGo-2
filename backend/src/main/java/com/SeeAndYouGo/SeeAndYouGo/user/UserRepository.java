package com.SeeAndYouGo.SeeAndYouGo.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Long countByNickname(String nickname);

    User findByEmail(String email);
    boolean existsByEmail(String email);
}