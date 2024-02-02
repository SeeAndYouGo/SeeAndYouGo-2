package com.SeeAndYouGo.SeeAndYouGo.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public boolean checkAvailableNickname(String nickname) {
        return userRepository.countByNickname(nickname) == 0;
    }

    @Transactional
    public void updateNickname(String email, String nickname) {
        User user = userRepository.findByEmail(email).get(0);
        user.changeNickname(nickname);
    }

    public String getNicknameByEmail(String email) {
        User user = userRepository.findByEmail(email).get(0);
        return user.getNickname() == null ? "익명" : user.getNickname();
    }

    public String findNickname(String email) {
        User user = userRepository.findByEmail(email).get(0);
        return user.getNickname() == null ? "익명" : user.getNickname();
    }
}
