package puj.app.service;

import puj.app.model.*;
import puj.app.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeacherService {
    private final SyllabusRepository syllabusRepo;
    private final RubricRepository rubricRepo;
    private final StorageService storage;

    public Syllabus uploadSyllabus(String courseId, Integer version, String title, MultipartFile file, String uploadedBy) {
        String id = UUID.randomUUID().toString();
        String key = "syllabi/" + courseId + "/v" + version + "-" + id + ".pdf";
        String uri = store(file, key);
        Syllabus s = Syllabus.builder()
                .id(id).courseId(courseId).version(version).title(title)
                .fileUri(uri).uploadedBy(uploadedBy).build();
        return syllabusRepo.save(s);
    }

    public Rubric uploadRubric(String courseId, String name, Integer version, MultipartFile file, String uploadedBy) {
        String id = UUID.randomUUID().toString();
        String key = "rubrics/" + courseId + "/" + name + "-v" + version + "-" + id + ".pdf";
        String uri = store(file, key);
        Rubric r = Rubric.builder()
                .id(id).courseId(courseId).name(name).version(version)
                .fileUri(uri).uploadedBy(uploadedBy).build();
        return rubricRepo.save(r);
    }

    private String store(MultipartFile file, String key) {
        try {
            return storage.upload(key, file.getInputStream(), file.getSize(), file.getContentType());
        } catch (Exception e) {
            throw new RuntimeException("Error almacenando archivo", e);
        }
    }
}
