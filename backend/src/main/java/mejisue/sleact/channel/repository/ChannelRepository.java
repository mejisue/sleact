package mejisue.sleact.channel.repository;

import mejisue.sleact.channel.domain.Channel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelRepository extends JpaRepository<Channel, Long> {
}
