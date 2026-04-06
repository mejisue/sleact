package mejisue.sleact.channelChat.repository;

import mejisue.sleact.channelChat.domain.ChannelChat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChannelChatRepository extends JpaRepository<ChannelChat, Long> {

    @Query("SELECT cc FROM ChannelChat cc JOIN FETCH cc.user WHERE cc.channel.id = :channelId")
    Page<ChannelChat> findByChannelId(@Param("channelId") Long channelId, Pageable pageable);
}
