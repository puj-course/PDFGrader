package puj.app.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="assignments",
        indexes=@Index(name="idx_assignments_course_due", columnList="course_id,due_at"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Assignment {
    @Id @Column(length=36)
    private String id;

    @Column(name="course_id", nullable=false, length=36)
    private String courseId;

    @Column(nullable=false, length=200)
    private String title;

    @Column(columnDefinition="TEXT")
    private String description;

    private Instant dueAt;

    private Instant publishedAt;

    @Column(name="created_by", nullable=false, length=36)
    private String createdBy;

    @Column(nullable=false)
    private Instant createdAt = Instant.now();
}
