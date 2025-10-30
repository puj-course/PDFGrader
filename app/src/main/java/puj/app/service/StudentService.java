package puj.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import puj.app.model.Assignment;
import puj.app.model.Submission;
import puj.app.model.SubmissionStatus;
import puj.app.repository.AssignmentRepository;
import puj.app.repository.SubmissionRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private final StorageService storage;
    private final SubmissionRepository submissionRepo;
    private final AssignmentRepository assignmentRepo;

    @Transactional
    public void uploadSubmission(String assignmentId, String studentId, MultipartFile file) {
        try {
            Assignment a = assignmentRepo.findById(assignmentId).orElseThrow();

            String original = (file.getOriginalFilename() == null) ? "entrega.pdf" : file.getOriginalFilename();
            String safeName = original.trim().replaceAll("\\s+", "_");
            String key = "submissions/" + a.getCourseId() + "/" + assignmentId + "/" + studentId
                    + "/" + UUID.randomUUID() + "-" + safeName;

            String uri = storage.upload(key, file.getInputStream(), file.getSize(), file.getContentType());
            log.info("[SUBMISSION] assignmentId={}, studentId={}, key={}, uri={}", assignmentId, studentId, key, uri);

            Optional<Submission> existing = submissionRepo.findByAssignmentIdAndStudentId(assignmentId, studentId);
            if (existing.isPresent()) {
                Submission s = existing.get();
                s.setStorageUri(uri);
                s.setStatus(SubmissionStatus.SUBMITTED);
                s.setCreatedAt(Instant.now());
                s.setGradedAt(null);
                s.setFinalScore(null);
                submissionRepo.save(s);
                log.info("[SUBMISSION] Re-subida actualizada id={}", s.getId());
            } else {
                Submission s = Submission.builder()
                        .id(UUID.randomUUID().toString())
                        .assignmentId(assignmentId)
                        .studentId(studentId)
                        .storageUri(uri)
                        .status(SubmissionStatus.SUBMITTED)
                        .createdAt(Instant.now())
                        .build();
                submissionRepo.save(s);
                log.info("[SUBMISSION] Primera subida creada id={}", s.getId());
            }
        } catch (Exception e) {
            log.error("Error subiendo entrega", e);
            throw new RuntimeException("Error subiendo entrega: " + e.getMessage(), e);
        }
    }
}
