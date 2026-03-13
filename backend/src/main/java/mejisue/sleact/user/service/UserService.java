package mejisue.sleact.user.service;

import lombok.RequiredArgsConstructor;
import mejisue.sleact.user.domain.User;
import mejisue.sleact.user.dto.UserSaveReqDto;
import mejisue.sleact.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void signup(UserSaveReqDto dto) {
        /** 이메일 중복 확인 **/
        validateDuplicatedEmail(dto);
        User newMember = User.builder()
                .nickname(dto.getNickname())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .build();
        userRepository.save(newMember);

    }

    private void validateDuplicatedEmail(UserSaveReqDto dto) {
        if(userRepository.findByEmail(dto.getEmail()).isPresent() ) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }
    }

}
