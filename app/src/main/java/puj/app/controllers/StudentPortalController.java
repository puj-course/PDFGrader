package puj.app.controllers;

import puj.app.DTOs.SubmissionUploadDTO;
import puj.app.repository.*;
import puj.app.security.CustomUserDetails;
import puj.app.service.StudentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student")
@PreAuthorize("hasRole('ALUMNO')")
public class StudentPortalController {

    private final EnrollmentRepository enrollmentRepo;
    private final CourseRepository courseRepo;
    private final AssignmentRepository assignmentRepo;
    private final SubmissionRepository submissionRepo;
    private final StudentService studentService;

    @GetMapping("/portal")
    public String portal(@AuthenticationPrincipal CustomUserDetails me, Model model) {
        var enrolls = enrollmentRepo.findByStudentId(me.getUserId());
        var courseIds = enrolls.stream().map(e -> e.getCourseId()).toList();
        var courses = courseRepo.findAllById(courseIds);

        var assignments = courseIds.isEmpty()
                ? List.of()
                : assignmentRepo.findByCourseIdInOrderByDueAtAsc(courseIds);

        var mySubmissions = submissionRepo.findByStudentIdOrderByCreatedAtDesc(me.getUserId());

        model.addAttribute("me", me);
        model.addAttribute("courses", courses);
        model.addAttribute("assignments", assignments);
        model.addAttribute("mySubmissions", mySubmissions);
        if (!model.containsAttribute("form")) model.addAttribute("form", new SubmissionUploadDTO());
        return "student/Portal";
    }

    @PostMapping("/upload")
    public String upload(@AuthenticationPrincipal CustomUserDetails me,
                         @Valid @ModelAttribute("form") SubmissionUploadDTO form,
                         BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.form", br);
            ra.addFlashAttribute("form", form);
            return "redirect:/student/portal";
        }
        studentService.uploadSubmission(form.getAssignmentId(), me.getUserId(), form.getFile());
        ra.addFlashAttribute("success","Entrega subida");
        return "redirect:/student/portal";
    }
}