package com.SeeAndYouGo.SeeAndYouGo.user;

import com.SeeAndYouGo.SeeAndYouGo.user.dto.NicknameCheckResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.user.dto.NicknameUpdateResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.user.dto.UserNicknameRequest;
import com.SeeAndYouGo.SeeAndYouGo.user.dto.UserResponseDto;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    private final UserService userService;

    @GetMapping("/nickname/check/{nickname}")
    public NicknameCheckResponseDto checkNicknameRedundancy(@PathVariable String nickname) {
        boolean redundancy = !userService.isNicknameCountZero(nickname);
        return NicknameCheckResponseDto.builder()
                                        .redundancy(redundancy).build();
    }

    @PutMapping("/nickname")
    public NicknameUpdateResponseDto changeNickname(@RequestBody UserNicknameRequest nicknameRequest,
                                                    @Parameter(hidden = true) @AuthenticationPrincipal String email){
        log.info("Request to change nickname for authenticated user to: {}", nicknameRequest.getNickname());
        String lastUpdateTime = userService.getLastUpdateTimeForNickname(email);

        boolean canUpdate = userService.canUpdateNickname(email);
        if(canUpdate){
            userService.updateNickname(email, nicknameRequest.getNickname());
            log.info("Successfully updated nickname for authenticated user.");
        } else {
            log.warn("Nickname update denied for authenticated user. Last update time: {}", lastUpdateTime);
        }

        return NicknameUpdateResponseDto.builder()
                .update(canUpdate)
                .lastUpdate(lastUpdateTime)
                .build();
    }

    @GetMapping("/nickname")
    public UserResponseDto getNickname(@Parameter(hidden = true) @AuthenticationPrincipal String email){
        String nickname = userService.getNicknameByEmail(email);
        return UserResponseDto.builder()
                .nickname(nickname)
                .build();
    }
}