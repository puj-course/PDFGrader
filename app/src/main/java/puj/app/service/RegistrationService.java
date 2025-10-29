package puj.app.service;

import puj.app.model.Role;
import puj.app.model.User;
import puj.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public User register(String email, String fullName, String rawPassword, Role role) {
        userRepo.findByEmail(email.toLowerCase()).ifPresent(u -> {
            throw new IllegalArgumentException("El correo ya está registrado.");
        });

        User u = User.builder()
                .id(UUID.randomUUID().toString()) // CHAR(36)
                .email(email.toLowerCase())
                .fullName(fullName)
                .passwordHash(encoder.encode(rawPassword))
                .role(role)
                .isActive(true)
                .build();

        return userRepo.save(u);
    }
}