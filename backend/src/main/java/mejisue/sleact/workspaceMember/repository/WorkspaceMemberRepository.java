package mejisue.sleact.workspaceMember.repository;


import mejisue.sleact.workspaceMember.domain.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {

    @Query("select wm from WorkspaceMember wm join fetch wm.workspace ws where wm.user.id = :userId")
    List<WorkspaceMember> findByUserId(@Param("userId") Long userId);

    @Query("select wm from WorkspaceMember wm join fetch wm.workspace ws join fetch wm.user u where u.id = :userId and ws.id = :workspaceId")
    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(@Param("workspaceId") Long workspaceId, @Param("userId") Long userId);

    @Query("select wm from WorkspaceMember wm join fetch wm.workspace ws join fetch wm.user u where u.email = :userEmail and ws.id = :workspaceId")
    Optional<WorkspaceMember> findByWorkspaceIdAndUserEmail(@Param("workspaceId") Long workspaceId, @Param("userEmail") String userEmail);

    @Query("select wm from WorkspaceMember wm join fetch wm.workspace join fetch wm.user where wm.user.id in :userIds")
    List<WorkspaceMember> findByUserIdIn(@Param("userIds") List<Long> userIds);
}
