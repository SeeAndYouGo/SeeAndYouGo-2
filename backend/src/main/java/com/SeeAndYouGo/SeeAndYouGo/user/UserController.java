package com.SeeAndYouGo.SeeAndYouGo.user;

import com.SeeAndYouGo.SeeAndYouGo.global.response.ApiResponse;
import com.SeeAndYouGo.SeeAndYouGo.user.dto.NicknameCheckResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.user.dto.NicknameUpdateResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.user.dto.UserNicknameRequest;
import com.SeeAndYouGo.SeeAndYouGo.user.dto.UserResponseDto;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    private static final String NICKNAME_UPDATED_MESSAGE = "닉네임이 변경되었습니다.";

    private final UserService userService;
    private final AuthorizationService authorizationService;

    @GetMapping("/nickname-availability")
    public NicknameCheckResponseDto checkNicknameRedundancy(@RequestParam("nickname") String nickname) {
        boolean redundancy = !userService.isNicknameCountZero(nickname);

        return NicknameCheckResponseDto.builder()
                                        .redundancy(redundancy).build();
    }

    @PutMapping("/me/nickname")
    public ApiResponse<NicknameUpdateResponseDto> changeNickname(@RequestBody UserNicknameRequest nicknameRequest,
                                                                 @Parameter(hidden = true) @AuthenticationPrincipal String email){
        authorizationService.getLoginUser(email);
        String lastUpdateTime = userService.getLastUpdateTimeForNickname(email);

        boolean canUpdate;
        if(canUpdate = userService.canUpdateNickname(email)){
            userService.updateNickname(email, nicknameRequest.getNickname());
        }

        NicknameUpdateResponseDto response = NicknameUpdateResponseDto.builder()
                .update(canUpdate)
                .lastUpdate(lastUpdateTime)
                .build();
        // 변경 주기 제한(update: false) 문구는 프론트 자체 토스트와 함께 정리할 예정이라 기본 문구를 유지한다.
        return canUpdate ? ApiResponse.success(response, NICKNAME_UPDATED_MESSAGE) : ApiResponse.success(response);
    }

    @GetMapping("/me")
    public UserResponseDto getNickname(@Parameter(hidden = true) @AuthenticationPrincipal String email){
        authorizationService.getLoginUser(email);
        String nickname = userService.getNicknameByEmail(email);

        return UserResponseDto.builder()
                .nickname(nickname)
                .build();
    }
}