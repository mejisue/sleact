package mejisue.sleact.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mejisue.sleact.common.domain.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class User extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String email;
    private String nickname;
    private String password;
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;



}
