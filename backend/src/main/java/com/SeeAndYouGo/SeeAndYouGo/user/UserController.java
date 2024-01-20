package com.SeeAndYouGo.SeeAndYouGo.user;

import com.SeeAndYouGo.SeeAndYouGo.OAuth.jwt.TokenProvider;
import com.SeeAndYouGo.SeeAndYouGo.user.dto.NicknameCheckResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    private final UserService userService;
    private final TokenProvider tokenProvider;

    @GetMapping("/nickname/check/{nickname}")
    public ResponseEntity<NicknameCheckResponseDto> checkNicknameRedundancy(@PathVariable String nickname) {
        boolean redundancy = !userService.checkAvailableNickname(nickname);

        NicknameCheckResponseDto nicknameCheckResponseDto = NicknameCheckResponseDto.builder()
                                                    .redundancy(redundancy).build();

        return ResponseEntity.ok(nicknameCheckResponseDto);
    }

    @PutMapping("/nickname")
    public ResponseEntity changeNickname(
            @RequestParam("token") String jwtToken,
            @RequestParam("nickname") String nickname){
        String email = tokenProvider.decode(jwtToken);
        userService.updateNickname(email, nickname);

        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/nickname/{token}")
    public ResponseEntity<UserResponseDto> getNickname(@PathVariable String token){
        String email = tokenProvider.decode(token);
        String nickname = userService.getNicknameByEmail(email);

        UserResponseDto userResponseDto = UserResponseDto.builder()
                                            .nickname(nickname)
                                    .build();

        return ResponseEntity.ok(userResponseDto);
    }
}
