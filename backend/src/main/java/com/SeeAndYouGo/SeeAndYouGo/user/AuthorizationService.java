package com.SeeAndYouGo.SeeAndYouGo.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthorizationService {
    private static final String LOGIN_USER_NOT_FOUND_MESSAGE = "사용자 정보를 찾을 수 없습니다. 다시 로그인해주세요.";

    private final UserReader userReader;

    /**
     * 로그인 사용자를 반환한다. 비로그인이면 401(AUTH_001), 토큰의 사용자가 없으면 404(USER_001).
     */
    public User getLoginUser(String email) {
        if (email == null || "none".equals(email)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        return userReader.getByEmail(email, LOGIN_USER_NOT_FOUND_MESSAGE);
    }

    public void assertAdmin(String email) {
        User user = getLoginUser(email);
        if (!user.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자 권한이 필요합니다.");
        }
    }
}
