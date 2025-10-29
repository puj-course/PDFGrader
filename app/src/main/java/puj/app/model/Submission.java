package puj.app.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="submissions",
        uniqueConstraints=@UniqueConstraint(name="uq_submissions_assignment_student", columnNames={"assignment_id","student_id"}),
        indexes=@Index(name="idx_submissions_status", columnList="assignment_id,status"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Submission {
    @Id @Column(length=36)
    private String id;

    @Column(name="assignment_id", nullable=false, length=36)
    private String assignmentId;

    @Column(name="student_id", nullable=false, length=36)
    private String studentId;

    @Column(nullable=false, columnDefinition="TEXT")
    private String storageUri;

    @Column(length=128)
    private String fileHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private SubmissionStatus status = SubmissionStatus.SUBMITTED;

    @Column(nullable=false)
    private Instant createdAt = Instant.now();

    private Instant gradedAt;

    private Double finalScore;
}
