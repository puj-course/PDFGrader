package puj.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import puj.app.model.Syllabus;

import java.util.List;
import java.util.Optional;

@Repository
public interface SyllabusRepository extends JpaRepository<Syllabus, String> {
    List<Syllabus> findByCourseIdOrderByVersionDesc(String courseId);
    Optional<Syllabus> findTopByCourseIdOrderByVersionDesc(String courseId);
}
