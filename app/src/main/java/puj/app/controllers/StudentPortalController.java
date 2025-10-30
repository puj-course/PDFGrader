package puj.app.controllers;

import puj.app.DTOs.SubmissionUploadDTO;
import puj.app.model.Assignment;
import puj.app.model.Course;
import puj.app.model.Submission;
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
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student")
@PreAuthorize("hasRole('ALUMNO')")
public class StudentPortalController {

    private final EnrollmentRepository enrollmentRepo;
    private final CourseRepository courseRepo;
    private final AssignmentRepository assignmentRepo;
    private final SubmissionRepository submissionRepo;
    private final UserRepository userRepo;
    private final StudentService studentService;

    @GetMapping("/portal")
    public String portal(@AuthenticationPrincipal CustomUserDetails me, Model model) {
        var user = userRepo.findById(me.getUserId()).orElseThrow();

        var enrolls = enrollmentRepo.findByStudentId(me.getUserId());
        List<String> courseIds = enrolls.stream().map(e -> e.getCourseId()).toList();
        List<Course> courses = courseRepo.findAllById(courseIds);

        List<Assignment> assignments = courseIds.isEmpty()
                ? List.<Assignment>of()
                : assignmentRepo.findByCourseIdInOrderByDueAtAsc(courseIds);

        Map<String, Course> courseById = courses.stream()
                .collect(Collectors.toMap(Course::getId, c -> c));

        model.addAttribute("me", me);
        model.addAttribute("user", user);
        model.addAttribute("courses", courses);
        model.addAttribute("assignments", assignments);
        model.addAttribute("courseById", courseById);
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

    @GetMapping("/grades")
    public String grades(@AuthenticationPrincipal CustomUserDetails me, Model model) {
        var enrolls = enrollmentRepo.findByStudentId(me.getUserId());
        List<String> courseIds = enrolls.stream().map(e -> e.getCourseId()).toList();
        List<Course> courses = courseRepo.findAllById(courseIds);

        List<Assignment> assignments = courseIds.isEmpty()
                ? List.<Assignment>of()
                : assignmentRepo.findByCourseIdInOrderByDueAtAsc(courseIds);

        List<Submission> mySubs = submissionRepo.findByStudentIdOrderByCreatedAtDesc(me.getUserId());
        Map<String, Submission> subByAssignment = mySubs.stream()
                .collect(Collectors.toMap(Submission::getAssignmentId, s -> s, (a, b) -> a));

        record Row(String courseCode, String courseName, String assignmentTitle,
                   String status, String nota, String fechaEnvio) {}

        List<Row> rows = new java.util.ArrayList<>();
        Map<String, Course> courseById = courses.stream()
                .collect(Collectors.toMap(Course::getId, c -> c));

        for (Assignment a : assignments) {
            var course = courseById.get(a.getCourseId());
            var sub = subByAssignment.get(a.getId());
            String status = (sub == null) ? "Sin enviar" : sub.getStatus().name();
            String nota = (sub != null && sub.getFinalScore() != null) ? String.valueOf(sub.getFinalScore()) : "—";
            String fechaEnvio = (sub != null && sub.getCreatedAt() != null) ? sub.getCreatedAt().toString() : "—";

            rows.add(new Row(
                    course != null ? course.getCode() : "",
                    course != null ? course.getName() : "",
                    a.getTitle(), status, nota, fechaEnvio
            ));
        }

        var user = userRepo.findById(me.getUserId()).orElseThrow();
        model.addAttribute("me", me);
        model.addAttribute("user", user);
        model.addAttribute("rows", rows);
        return "student/Grades";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal CustomUserDetails me, Model model) {
        var user = userRepo.findById(me.getUserId()).orElseThrow();
        model.addAttribute("me", me);
        model.addAttribute("user", user);
        return "student/Profile"; // Asegúrate de tener templates/student/Profile.html
    }
}
