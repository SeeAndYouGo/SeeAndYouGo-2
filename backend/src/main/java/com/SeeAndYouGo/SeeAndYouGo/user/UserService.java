package com.SeeAndYouGo.SeeAndYouGo.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public boolean isNicknameCountZero(String nickname) {
        return userRepository.countByNickname(nickname) == 0;
    }

    @Transactional
    public void updateNickname(String email, String nickname) {
        User user = userRepository.findByEmail(email);
        user.changeNickname(nickname);
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

        return user.canUpdateNickname(LocalDateTime.now());
    }

    public String getLastUpdateTimeForNickname(String email) {
        User user = userRepository.findByEmail(email);

        return user.getLastUpdateTime().toLocalDate().toString();
    }
}