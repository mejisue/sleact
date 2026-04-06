package mejisue.sleact.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSaveReqDto {

    private String email;
    private String password;
    private String nickname;
}

