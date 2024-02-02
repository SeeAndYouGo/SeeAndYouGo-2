package com.SeeAndYouGo.SeeAndYouGo.Keyword;

import com.SeeAndYouGo.SeeAndYouGo.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserKeywordRepository extends JpaRepository<UserKeyword, Long> {

    List<Keyword> findByUser(User user);

    void deleteByUserAndKeyword(User user, Keyword keyword);
}