package mejisue.sleact.workspace.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class InviteMemberDto {
    private String email;

    /** 추가로 입장시킬 채널 이름 목록 (선택사항, 기본채널 제외) **/
    private List<String> channelNames = Collections.emptyList();
}
