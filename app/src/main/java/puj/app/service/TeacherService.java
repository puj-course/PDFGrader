package puj.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import puj.app.model.Rubric;
import puj.app.model.Syllabus;
import puj.app.repository.RubricRepository;
import puj.app.repository.SyllabusRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final StorageService storage;
    private final SyllabusRepository syllabusRepo;
    private final RubricRepository rubricRepo;

    @Transactional
    public void uploadSyllabusAutoVersion(String courseId, String title,
                                          MultipartFile file, String uploadedBy) {
        try {
            // busca última versión
            Integer nextVersion = syllabusRepo
                    .findTopByCourseIdOrderByVersionDesc(courseId)
                    .map(Syllabus::getVersion)
                    .map(v -> v + 1)
                    .orElse(1);

            String key = "syllabi/" + courseId + "/v" + nextVersion + "/" + UUID.randomUUID() + ".pdf";
            String uri = storage.upload(key, file.getInputStream(), file.getSize(), file.getContentType());

            var s = Syllabus.builder()
                    .id(UUID.randomUUID().toString())
                    .courseId(courseId)
                    .version(nextVersion)
                    .title(title)
                    .fileUri(uri)
                    .uploadedBy(uploadedBy)
                    .uploadedAt(Instant.now())
                    .isActive(true)
                    .build();
            syllabusRepo.save(s);
        } catch (Exception e) {
            throw new RuntimeException("Error subiendo temario: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void uploadRubric(String courseId, String name, Integer version,
                             MultipartFile file, String uploadedBy) {
        try {
            String safeName = name.trim().replaceAll("\\s+", "_");
            String key = "rubrics/" + courseId + "/" + safeName + "/v" + version + "/" + UUID.randomUUID() + ".pdf";
            String uri = storage.upload(key, file.getInputStream(), file.getSize(), file.getContentType());

            var r = Rubric.builder()
                    .id(UUID.randomUUID().toString())
                    .courseId(courseId)
                    .name(name)
                    .version(version)
                    .fileUri(uri)
                    .uploadedBy(uploadedBy)
                    .uploadedAt(Instant.now())
                    .isActive(true)
                    .build();
            rubricRepo.save(r);
        } catch (Exception e) {
            throw new RuntimeException("Error subiendo rúbrica: " + e.getMessage(), e);
        }
    }
}
