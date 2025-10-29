package puj.app.controllers;

import puj.app.DTOs.SubmissionUploadDTO;
import puj.app.security.CustomUserDetails;
import puj.app.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student")
@PreAuthorize("hasRole('ALUMNO')")
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/upload-submission")
    public String formSubmission(@RequestParam(value="assignmentId", required=false) String assignmentId,
                                 Model model) {
        var dto = new SubmissionUploadDTO();
        dto.setAssignmentId(assignmentId);
        model.addAttribute("form", dto);
        return "student/UploadSubmission";
    }

    @PostMapping("/upload-submission")
    public String uploadSubmission(@Valid @ModelAttribute("form") SubmissionUploadDTO form,
                                   BindingResult br,
                                   @org.springframework.security.core.annotation.AuthenticationPrincipal CustomUserDetails me,
                                   RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.form", br);
            ra.addFlashAttribute("form", form);
            return "redirect:/student/upload-submission";
        }
        studentService.uploadSubmission(form.getAssignmentId(), me.getUserId(), form.getFile());
        ra.addFlashAttribute("success","Entrega subida");
        return "redirect:/dashboard";
    }
}
