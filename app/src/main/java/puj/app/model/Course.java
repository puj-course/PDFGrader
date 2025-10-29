package puj.app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="courses",
        uniqueConstraints=@UniqueConstraint(name="uq_courses_code", columnNames="code"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Course {

    @Id @Column(length=36)
    private String id;

    @Column(nullable=false, length=50)
    private String code;

    @Column(nullable=false, length=200)
    private String name;

    @Column(nullable=false, length=36)
    private String createdBy;

    @Column(nullable=false)
    private boolean isActive = true;
}