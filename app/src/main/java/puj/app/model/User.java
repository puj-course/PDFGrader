package puj.app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="users",
        uniqueConstraints = @UniqueConstraint(name="uq_users_email", columnNames="email"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable=false, length=255)
    private String email;

    @Column(nullable=false, length=200)
    private String fullName;

    @Column(name="password_hash", nullable=false, length=255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private Role role;

    @Column(nullable=false)
    private boolean isActive = true;
}
