package mejisue.sleact.dm.dto;

import lombok.Data;
import mejisue.sleact.user.dto.UserResDto;

import java.time.LocalDateTime;

@Data
public class DmDto {
    private Long id;
    private Long senderId;
    private UserResDto sender;
    private Long receiverId;
    private UserResDto receiver;
    private String content;
    private LocalDateTime createdAt;
}
