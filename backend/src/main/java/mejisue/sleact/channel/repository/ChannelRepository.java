package mejisue.sleact.channel.repository;

import mejisue.sleact.channel.domain.Channel;
import mejisue.sleact.workspace.domain.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChannelRepository extends JpaRepository<Channel, Long> {

    Optional<Channel> findByWorkspaceAndName(Workspace workspace, String name);


    @Query("SELECT c FROM Channel c JOIN FETCH c.workspace WHERE c.workspace.id = :workspaceId AND c.name = :channelName")
    Optional<Channel> findChannelWithWorkspaceById(
            @Param("workspaceId") Long workspaceId,
            @Param("channelName") String channelName);
}
