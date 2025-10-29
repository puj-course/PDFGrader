package puj.app.service;

import puj.app.model.*;
import puj.app.repository.SubmissionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final SubmissionRepository submissionRepo;
    private final StorageService storage;

    public Submission uploadSubmission(String assignmentId, String studentId, MultipartFile file) {
        String id = UUID.randomUUID().toString();
        String key = "submissions/" + assignmentId + "/" + studentId + "-" + id + ".pdf";
        String uri = store(file, key);
        Submission s = Submission.builder()
                .id(id).assignmentId(assignmentId).studentId(studentId)
                .storageUri(uri).status(SubmissionStatus.SUBMITTED)
                .build();
        return submissionRepo.save(s);
    }

    private String store(MultipartFile file, String key) {
        try {
            return storage.upload(key, file.getInputStream(), file.getSize(), file.getContentType());
        } catch (Exception e) {
            throw new RuntimeException("Error almacenando archivo", e);
        }
    }
}
