package mejisue.sleact.workspaceMember.repository;


import mejisue.sleact.workspaceMember.domain.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {

    @Query("select wm from WorkspaceMember wm join fetch wm.workspace ws where wm.user.id = :userId")
    List<WorkspaceMember> findByUserId(@Param("userId") Long userId);
}

