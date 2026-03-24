package mejisue.sleact.channel.repository;

import mejisue.sleact.channel.domain.Channel;
import mejisue.sleact.workspace.domain.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChannelRepository extends JpaRepository<Channel, Long> {

    Optional<Channel> findByWorkspaceAndName(Workspace workspace, String name);
    List<Channel> findChannelsByWorkspaceId(Long workspaceId);
}
