package mejisue.sleact.chat.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PresenceResDto {

    private final String email;
    private final String status;
}
