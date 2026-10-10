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
        User user = userReader.getByEmail(email, "닉네임을 변경할 사용자 정보를 찾을 수 없습니다. 다시 로그인해주세요.");
        user.changeNickname(nickname);
    }

    public String getNicknameByEmail(String email) {
        return userReader.findByEmail(email)
                .map(user -> user.getNickname() != null ? user.getNickname() : "익명")
                .orElse("익명");
    }

    public boolean canUpdateNickname(String email) {
        User user = userReader.getByEmail(email, "닉네임 변경 가능 여부를 확인할 사용자 정보를 찾을 수 없습니다. 다시 로그인해주세요.");
        return user.canUpdateNickname(LocalDateTime.now());
    }

    public String getLastUpdateTimeForNickname(String email) {
        User user = userReader.getByEmail(email, "닉네임 변경 이력을 조회할 사용자 정보를 찾을 수 없습니다. 다시 로그인해주세요.");
        return user.getLastUpdateTime().toLocalDate().toString();
    }
}