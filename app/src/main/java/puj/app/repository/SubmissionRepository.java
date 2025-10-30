package puj.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puj.app.model.Submission;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, String> {
    Optional<Submission> findByAssignmentIdAndStudentId(String assignmentId, String studentId);
    List<Submission> findByStudentIdOrderByCreatedAtDesc(String studentId);
    List<Submission> findByAssignmentId(String assignmentId);
}
