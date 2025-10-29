package puj.app.model;


import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="enrollments",
        uniqueConstraints=@UniqueConstraint(name="uq_enrollments_course_student", columnNames={"course_id","student_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Enrollment {
    @Id @Column(length=36)
    private String id;

    @Column(name="course_id", nullable=false, length=36)
    private String courseId;

    @Column(name="student_id", nullable=false, length=36)
    private String studentId;

    @Column(nullable=false)
    private Instant enrolledAt = Instant.now();
}