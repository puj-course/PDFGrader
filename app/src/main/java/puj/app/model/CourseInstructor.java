package puj.app.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="course_instructors",
        uniqueConstraints=@UniqueConstraint(name="uq_course_instructors_course_user", columnNames={"course_id","user_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CourseInstructor {

    @Id @Column(length=36)
    private String id;

    @Column(name="course_id", nullable=false, length=36)
    private String courseId;

    @Column(name="user_id", nullable=false, length=36)
    private String userId;

    @Column(nullable=false)
    private Instant assignedAt = Instant.now();
}