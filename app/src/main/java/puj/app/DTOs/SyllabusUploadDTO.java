package puj.app.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SyllabusUploadDTO {
    @NotBlank
    private String courseId;
    private String title;
    @NotNull
    private MultipartFile file;
}
