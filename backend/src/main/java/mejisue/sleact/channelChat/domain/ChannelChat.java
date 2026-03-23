package mejisue.sleact.channelChat.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mejisue.sleact.channel.domain.Channel;
import mejisue.sleact.common.domain.BaseEntity;
import mejisue.sleact.user.domain.User;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ChannelChat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, name = "channel_chat_id")
    private Long id;
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}
