package puj.app.DTOs;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RubricUploadDTO {
    @NotBlank private String courseId;
    @NotBlank @Size(max=200) private String name;
    @NotNull  private Integer version;
    @NotNull  private MultipartFile file;
}
