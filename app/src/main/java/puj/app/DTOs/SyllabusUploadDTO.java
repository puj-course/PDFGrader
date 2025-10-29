package puj.app.DTOs;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SyllabusUploadDTO {
    @NotBlank private String courseId;
    @NotNull  private Integer version;
    @Size(max=200) private String title;
    @NotNull private MultipartFile file;
}