package puj.app.repository;

import puj.app.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface CourseInstructorRepository extends JpaRepository<CourseInstructor, String> {
    List<CourseInstructor> findByUserId(String userId);
    List<CourseInstructor> findByCourseId(String courseId);
}
