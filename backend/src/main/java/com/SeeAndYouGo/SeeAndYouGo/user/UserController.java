package com.SeeAndYouGo.SeeAndYouGo.user;

import com.SeeAndYouGo.SeeAndYouGo.aop.ValidateToken;
import com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt.TokenProvider;
import com.SeeAndYouGo.SeeAndYouGo.user.dto.NicknameCheckResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.user.dto.NicknameUpdateResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.user.dto.UserNicknameRequest;
import com.SeeAndYouGo.SeeAndYouGo.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    private final UserService userService;
    private final TokenProvider tokenProvider;

    @GetMapping("/nickname/check/{nickname}")
    public NicknameCheckResponseDto checkNicknameRedundancy(@PathVariable String nickname) {
        boolean redundancy = !userService.isNicknameCountZero(nickname);

        return NicknameCheckResponseDto.builder()
                                        .redundancy(redundancy).build();
    }

    @PutMapping("/nickname")
    public NicknameUpdateResponseDto changeNickname(@RequestBody UserNicknameRequest nicknameRequest){
        String email = tokenProvider.decodeToEmail(nicknameRequest.getToken());
        String lastUpdateTime = userService.getLastUpdateTimeForNickname(email);

        boolean canUpdate;
        if(canUpdate = userService.canUpdateNickname(email)){
            userService.updateNickname(email, nicknameRequest.getNickname());
        }

        return NicknameUpdateResponseDto.builder()
                .update(canUpdate)
                .lastUpdate(lastUpdateTime)
                .build();
    }

    @GetMapping("/nickname/{token}")
    @ValidateToken
    public UserResponseDto getNickname(@PathVariable(value = "token") String tokenId){
        String email = tokenProvider.decodeToEmail(tokenId);
        String nickname = userService.getNicknameByEmail(email);

        return UserResponseDto.builder()
                .nickname(nickname)
                .build();
    }
}