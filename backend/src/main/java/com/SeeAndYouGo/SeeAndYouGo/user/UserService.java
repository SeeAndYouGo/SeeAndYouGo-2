package com.SeeAndYouGo.SeeAndYouGo.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserReader userReader;

    public boolean isNicknameCountZero(String nickname) {
        return userRepository.countByNickname(nickname) == 0;
    }

    @Transactional
    public void updateNickname(String email, String nickname) {
        User user = userReader.getByEmail(email);
        user.changeNickname(nickname);
    }

    public String getNicknameByEmail(String email) {
        return userReader.findByEmail(email)
                .map(user -> user.getNickname() != null ? user.getNickname() : "익명")
                .orElse("익명");
    }

    public boolean canUpdateNickname(String email) {
        User user = userReader.getByEmail(email);
        return user.canUpdateNickname(LocalDateTime.now());
    }

    public String getLastUpdateTimeForNickname(String email) {
        User user = userReader.getByEmail(email);
        return user.getLastUpdateTime().toLocalDate().toString();
    }
}