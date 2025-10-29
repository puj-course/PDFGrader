package puj.app.DTOs;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SubmissionUploadDTO {
    @NotBlank private String assignmentId;
    @NotNull  private MultipartFile file;
}