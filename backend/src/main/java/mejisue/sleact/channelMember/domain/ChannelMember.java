package mejisue.sleact.channelMember.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mejisue.sleact.channel.domain.Channel;
import mejisue.sleact.common.domain.BaseEntity;
import mejisue.sleact.user.domain.User;

import static jakarta.persistence.FetchType.LAZY;


@Entity
@Builder
@AllArgsConstructor
@Getter
@NoArgsConstructor
public class ChannelMember extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}
