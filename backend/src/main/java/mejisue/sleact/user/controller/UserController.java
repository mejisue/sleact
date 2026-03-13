package mejisue.sleact.user.controller;

import lombok.RequiredArgsConstructor;
import mejisue.sleact.user.dto.UserSaveReqDto;
import mejisue.sleact.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin
public class UserController {
    private final UserService userService;

    /**
     * 회원가입
     * UserSaveReqDto : email, password, nickname
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signupMember(@RequestBody UserSaveReqDto memberDto) {
        userService.signup(memberDto);
        return ResponseEntity.ok().body("ok");
    }
}
