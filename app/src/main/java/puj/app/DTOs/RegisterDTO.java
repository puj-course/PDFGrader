package puj.app.DTOs;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RegisterDTO {
    @NotBlank @Email @Size(max=255)
    private String email;

    @NotBlank @Size(max=200)
    private String fullName;

    @NotBlank @Size(min=8, max=100)
    private String password;
}
