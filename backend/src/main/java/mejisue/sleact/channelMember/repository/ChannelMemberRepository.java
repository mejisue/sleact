package mejisue.sleact.channelMember.repository;

import mejisue.sleact.channelMember.domain.ChannelMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChannelMemberRepository extends JpaRepository<ChannelMember, Long> {

    @Query("select cm from ChannelMember cm join fetch cm.channel c where c.workspace.id = :workspaceId and cm.user.email = :email")
    List<ChannelMember> findByWorkspaceIdAndUserEmail(@Param("workspaceId") Long workspaceId, @Param("email") String email);

    @Query("select cm from ChannelMember cm where cm.channel.id = :channelId and cm.user.email = :email")
    Optional<ChannelMember> findByChannelIdAndUserEmail(@Param("channelId") Long channelId, @Param("email") String email);

    @Query("select cm from ChannelMember cm join fetch cm.user where cm.channel.id = :channelId")
    List<ChannelMember> findByChannelIdWithUser(@Param("channelId") Long channelId);
}
