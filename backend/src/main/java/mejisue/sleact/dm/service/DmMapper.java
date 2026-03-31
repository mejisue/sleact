package mejisue.sleact.dm.service;

import mejisue.sleact.dm.domain.Dm;
import mejisue.sleact.dm.dto.DmDto;
import mejisue.sleact.user.domain.User;
import mejisue.sleact.user.dto.UserResDto;
import org.springframework.stereotype.Component;

@Component
public class DmMapper {

    public DmDto toIDMDto(Dm dm) {
        DmDto idmDto = new DmDto();
        idmDto.setId(dm.getId());
        idmDto.setSenderId(dm.getSender().getId());
        idmDto.setSender(toUserResDto(dm.getSender()));
        idmDto.setReceiverId(dm.getReceiver().getId());
        idmDto.setReceiver(toUserResDto(dm.getReceiver()));
        idmDto.setContent(dm.getContent());
        idmDto.setCreatedAt(dm.getCreatedAt());
        return idmDto;
    }

    private UserResDto toUserResDto(User user) {
        UserResDto dto = new UserResDto();
        dto.setId(user.getId());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
