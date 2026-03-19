package mejisue.sleact.user.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import mejisue.sleact.user.domain.User;
import mejisue.sleact.user.dto.UserLoginReqDto;
import mejisue.sleact.user.dto.UserSaveReqDto;
import mejisue.sleact.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(UserSaveReqDto dto) {
        /** 이메일 중복 확인 **/
        validateDuplicatedEmail(dto);
        User newMember = User.builder()
                .nickname(dto.getNickname())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();
        userRepository.save(newMember);

    }

    public User login(UserLoginReqDto memberLoginDto) {
        User member = userRepository.findByEmail(memberLoginDto.getEmail()).orElseThrow(
                () -> new EntityNotFoundException("존재하지 않는 이메일입니다."));
        if(!passwordEncoder.matches(memberLoginDto.getPassword(), member.getPassword())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }
        return member;
    }

    private void validateDuplicatedEmail(UserSaveReqDto dto) {
        if(userRepository.findByEmail(dto.getEmail()).isPresent() ) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }
    }

}
