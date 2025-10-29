package puj.app.repository;

import puj.app.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, String> {
    List<Submission> findByAssignmentIdOrderByCreatedAtDesc(String assignmentId);
}