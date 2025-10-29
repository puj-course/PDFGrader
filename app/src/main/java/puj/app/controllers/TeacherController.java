package puj.app.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import puj.app.DTOs.*;
import puj.app.model.Role;
import puj.app.security.CustomUserDetails;
import puj.app.service.TeacherService;
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
@RequestMapping("/teacher")
@PreAuthorize("hasRole('PROFESOR')")
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping("/upload-syllabus")
    public String formSyllabus(Model model) {
        if (!model.containsAttribute("form")) model.addAttribute("form", new SyllabusUploadDTO());
        return "teacher/UploadSyllabus";
    }

    @PostMapping("/upload-syllabus")
    public String uploadSyllabus(@AuthenticationPrincipal CustomUserDetails me,
                                 @Valid @ModelAttribute("form") SyllabusUploadDTO form,
                                 BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.form", br);
            ra.addFlashAttribute("form", form);
            return "redirect:/teacher/upload-syllabus";
        }
        teacherService.uploadSyllabus(form.getCourseId(), form.getVersion(), form.getTitle(),
                form.getFile(), me.getUserId());
        ra.addFlashAttribute("success","Temario subido");
        return "redirect:/dashboard";
    }

    @GetMapping("/upload-rubric")
    public String formRubric(Model model) {
        if (!model.containsAttribute("form")) model.addAttribute("form", new RubricUploadDTO());
        return "teacher/UploadRubric";
    }

    @PostMapping("/upload-rubric")
    public String uploadRubric(@AuthenticationPrincipal CustomUserDetails me,
                               @Valid @ModelAttribute("form") RubricUploadDTO form,
                               BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.form", br);
            ra.addFlashAttribute("form", form);
            return "redirect:/teacher/upload-rubric";
        }
        teacherService.uploadRubric(form.getCourseId(), form.getName(), form.getVersion(),
                form.getFile(), me.getUserId());
        ra.addFlashAttribute("success","Rúbrica subida");
        return "redirect:/dashboard";
    }
}

