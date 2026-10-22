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
    private static final String NICKNAME_CHANGE_LIMITED_MESSAGE =
            String.format("닉네임은 %d일에 한 번 변경할 수 있습니다.", User.NICKNAME_CHANGE_INTERVAL_DAYS);

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
        return ApiResponse.success(response, canUpdate ? NICKNAME_UPDATED_MESSAGE : NICKNAME_CHANGE_LIMITED_MESSAGE);
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