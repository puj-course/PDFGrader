package puj.app.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="syllabi",
        uniqueConstraints=@UniqueConstraint(name="uq_syllabi_course_version", columnNames={"course_id","version"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Syllabus {
    @Id @Column(length=36)
    private String id;

    @Column(name="course_id", nullable=false, length=36)
    private String courseId;

    @Column(nullable=false)
    private Integer version;

    @Column(length=200)
    private String title;

    @Column(nullable=false, columnDefinition="TEXT")
    private String fileUri;

    @Column(name="uploaded_by", nullable=false, length=36)
    private String uploadedBy;

    @Column(nullable=false)
    private Instant uploadedAt = Instant.now();

    @Column(nullable=false)
    private boolean isActive = true;
}