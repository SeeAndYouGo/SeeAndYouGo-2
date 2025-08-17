package com.SeeAndYouGo.SeeAndYouGo.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public boolean isNicknameCountZero(String nickname) {
        return userRepository.countByNickname(nickname) == 0;
    }

    @Transactional
    public void updateNickname(String email, String nickname) {
        log.info("Updating nickname to '{}' for a user.", nickname);
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.changeNickname(nickname);
            log.info("Nickname updated successfully for user ID: {}", user.getId());
        } else {
            log.error("User not found with the provided email for nickname update.");
            throw new javax.persistence.EntityNotFoundException("User not found with the provided email");
        }
    }

    public String getNicknameByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return user == null ? "익명" : user.getNickname();
    }

    public String findNickname(String email) {
        User user = userRepository.findByEmail(email);
        return user.getNickname() == null ? "익명" : user.getNickname();
    }

    public boolean canUpdateNickname(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            log.warn("Cannot check nickname update possibility, user not found.");
            return false;
        }
        return user.canUpdateNickname(LocalDateTime.now());
    }

    public String getLastUpdateTimeForNickname(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            log.warn("Cannot get last update time, user not found.");
            return null;
        }
        return user.getLastUpdateTime().toLocalDate().toString();
    }
}