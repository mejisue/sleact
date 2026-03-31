package mejisue.sleact.dm.repository;

import mejisue.sleact.dm.domain.Dm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DmRepository extends JpaRepository<Dm, Long> {

    @Query("SELECT d FROM Dm d WHERE d.workspace.id = :workspaceId " +
           "AND ((d.sender.id = :userA AND d.receiver.id = :userB) " +
           "OR (d.sender.id = :userB AND d.receiver.id = :userA)) " +
           "ORDER BY d.createdAt DESC")
    Page<Dm> findDmsBetweenUsers(
            @Param("workspaceId") Long workspaceId,
            @Param("userA") Long userAId,
            @Param("userB") Long userBId,
            Pageable pageable);
}
