package mejisue.sleact.dm.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mejisue.sleact.common.domain.BaseEntity;
import mejisue.sleact.user.domain.User;
import mejisue.sleact.workspace.domain.Workspace;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Dm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String content;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;

}
