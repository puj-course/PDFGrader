package puj.app.repository;

import puj.app.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
import puj.app.model.Role;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    List<User> findByRoleAndIsActive(Role role, boolean isActive);
    List<User> findByIdIn(List<String> ids);
}