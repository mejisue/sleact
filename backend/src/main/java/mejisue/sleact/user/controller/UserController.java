package mejisue.sleact.user.controller;

import lombok.RequiredArgsConstructor;
import mejisue.sleact.common.auth.JwtTokenProvider;
import mejisue.sleact.user.domain.User;
import mejisue.sleact.user.dto.UserLoginReqDto;
import mejisue.sleact.user.dto.UserResDto;
import mejisue.sleact.user.dto.UserSaveReqDto;
import mejisue.sleact.user.service.PasswordResetService;
import mejisue.sleact.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin
public class UserController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordResetService passwordResetService;

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
    public ResponseEntity<?> loginMember(@RequestBody UserLoginReqDto memberLoginDto, HttpServletResponse response) {
        User member = userService.login(memberLoginDto);
        String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());
        UserResDto userInfo = userService.getUser(member.getEmail());

        Cookie cookie = new Cookie("token", jwtToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24); // 24시간
        // cookie.setSecure(true); // HTTPS 환경에서 활성화
        response.addCookie(cookie);

        return ResponseEntity.ok().body(userInfo);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok().body("ok");
    }

    /**
     * 현재 로그인 유저 정보 조회
     */
    @GetMapping
    public ResponseEntity<UserResDto> getUser(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getUser(userDetails.getUsername()));
    }

    /**
     * 비밀번호 재설정 링크 요청
     */
    @PostMapping("/password-reset/request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> body) {
        passwordResetService.requestPasswordReset(body.get("email"));
        return ResponseEntity.ok().body("ok");
    }

    /**
     * 비밀번호 재설정 확인
     */
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        passwordResetService.resetPassword(body.get("token"), body.get("password"));
        return ResponseEntity.ok().body("ok");
    }

}
