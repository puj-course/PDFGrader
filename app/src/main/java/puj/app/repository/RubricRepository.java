package puj.app.repository;

import puj.app.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface RubricRepository extends JpaRepository<Rubric, String> {
    List<Rubric> findByCourseIdOrderByVersionDesc(String courseId);
}