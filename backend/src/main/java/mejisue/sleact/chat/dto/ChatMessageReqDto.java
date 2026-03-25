package mejisue.sleact.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageReqDto {

    private String workspace;
    private String content;
    private String senderEmail;
}
