package puj.app.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="rubrics",
        uniqueConstraints=@UniqueConstraint(name="uq_rubrics_course_name_version", columnNames={"course_id","name","version"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Rubric {
    @Id @Column(length=36)
    private String id;

    @Column(name="course_id", nullable=false, length=36)
    private String courseId;

    @Column(nullable=false, length=200)
    private String name;

    @Column(nullable=false)
    private Integer version;

    @Column(nullable=false, columnDefinition="TEXT")
    private String fileUri;

    @Column(name="uploaded_by", nullable=false, length=36)
    private String uploadedBy;

    @Column(nullable=false)
    private Instant uploadedAt = Instant.now();

    @Column(nullable=false)
    private boolean isActive = true;
}