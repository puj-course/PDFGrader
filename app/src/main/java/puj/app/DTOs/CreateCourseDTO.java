package puj.app.DTOs;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CreateCourseDTO {
    @NotBlank @Size(max=50)
    private String code;

    @NotBlank @Size(max=200)
    private String name;

    // @NotEmpty
    private List<String> studentIds = new ArrayList<>();
}
