package mejisue.sleact.user.controller;

import lombok.RequiredArgsConstructor;
import mejisue.sleact.common.auth.JwtTokenProvider;
import mejisue.sleact.user.domain.User;
import mejisue.sleact.user.dto.UserLoginReqDto;
import mejisue.sleact.user.dto.UserSaveReqDto;
import mejisue.sleact.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin
public class UserController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입
     * UserSaveReqDto : email, password, nickname
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signupMember(@RequestBody UserSaveReqDto memberDto) {
        userService.signup(memberDto);
        return ResponseEntity.ok().body("ok");
    }

    /**
     * 로그인
     * UserLoginReqDto : email, password
     */

    @PostMapping("/login")
    public ResponseEntity<?> loginMember(@RequestBody UserLoginReqDto memberLoginDto) {
        // email, password 검증
        User member = userService.login(memberLoginDto);
        // 일치할 경우 access 토큰 발행
        String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());
        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("token", jwtToken);
        return ResponseEntity.ok().body(loginInfo);
    }



}
