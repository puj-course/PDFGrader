package puj.app.controllers;

import puj.app.DTOs.*;
import puj.app.model.*;
import puj.app.repository.*;
import puj.app.security.CustomUserDetails;
import puj.app.service.TeacherService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/teacher")
@PreAuthorize("hasRole('PROFESOR')")
public class TeacherPortalController {

    private final CourseInstructorRepository courseInstructorRepo;
    private final CourseRepository courseRepo;
    private final AssignmentRepository assignmentRepo;
    private final SyllabusRepository syllabusRepo;
    private final RubricRepository rubricRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final UserRepository userRepo;
    private final SubmissionRepository submissionRepo;

    private final TeacherService teacherService;

    @GetMapping("/portal")
    public String portal(@AuthenticationPrincipal CustomUserDetails me, Model model) {
        var teaches = courseInstructorRepo.findByUserId(me.getUserId());
        var courseIds = teaches.stream().map(CourseInstructor::getCourseId).toList();
        var courses = courseRepo.findAllById(courseIds);
        var user = userRepo.findById(me.getUserId()).orElseThrow();

        var assignmentsByCourse = courses.stream().collect(Collectors.toMap(
                Course::getId,
                c -> assignmentRepo.findByCourseIdOrderByDueAtAsc(c.getId())
        ));

        model.addAttribute("me", me);
        model.addAttribute("user", user);
        model.addAttribute("courses", courses);
        model.addAttribute("assignmentsByCourse", assignmentsByCourse);
        return "teacher/Portal";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal CustomUserDetails me, Model model) {
        var user = userRepo.findById(me.getUserId()).orElseThrow();
        model.addAttribute("me", me);
        model.addAttribute("user", user);
        return "teacher/Profile";
    }

    @GetMapping("/course/new")
    public String newCourseForm(@AuthenticationPrincipal CustomUserDetails me, Model model) {
        var alumnos = userRepo.findByRoleAndIsActive(Role.ALUMNO, true);
        if (!model.containsAttribute("form")) model.addAttribute("form", new CreateCourseDTO());
        model.addAttribute("alumnos", alumnos);
        return "teacher/CourseNew";
    }

    @PostMapping("/course")
    @Transactional
    public String createCourse(@AuthenticationPrincipal CustomUserDetails me,
                               @Valid @ModelAttribute("form") CreateCourseDTO form,
                               BindingResult br,
                               RedirectAttributes ra) {

        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.form", br);
            ra.addFlashAttribute("form", form);
            return "redirect:/teacher/course/new";
        }

        // 1) Crear curso
        var course = Course.builder()
                .id(UUID.randomUUID().toString())
                .code(form.getCode())
                .name(form.getName())
                .createdBy(me.getUserId())
                .isActive(true)
                .build();
        courseRepo.saveAndFlush(course); // <- flush temprano para garantizar FK

        // 2) Vincular profesor como instructor (seteo explícito de fecha)
        var ci = CourseInstructor.builder()
                .id(UUID.randomUUID().toString())
                .courseId(course.getId())
                .userId(me.getUserId())
                .assignedAt(Instant.now())
                .build();
        courseInstructorRepo.save(ci);

        // 3) Matricular alumnos seleccionados
        var selected = (form.getStudentIds() == null) ? List.<String>of() : form.getStudentIds();
        for (var sid : selected) {
            if (!enrollmentRepo.existsByCourseIdAndStudentId(course.getId(), sid)) {
                var e = Enrollment.builder()
                        .id(UUID.randomUUID().toString())
                        .courseId(course.getId())
                        .studentId(sid)
                        .enrolledAt(Instant.now())
                        .build();
                enrollmentRepo.save(e);
            }
        }

        // 4) Redirect robusto con expansión de path-variable
        ra.addFlashAttribute("success", "Curso creado" + (selected.isEmpty() ? "" : " y alumnos matriculados"));
        ra.addAttribute("courseId", course.getId());
        return "redirect:/teacher/course/{courseId}";

    }


    @GetMapping("/course/{courseId}")
    public String courseDetail(@PathVariable String courseId,
                               @AuthenticationPrincipal CustomUserDetails me,
                               Model model) {
        var course = courseRepo.findById(courseId).orElseThrow();
        // Verificar que el profe sea instructor de este curso
        var isInstructor = courseInstructorRepo.findByUserId(me.getUserId())
                .stream().anyMatch(ci -> ci.getCourseId().equals(courseId));
        if (!isInstructor) throw new RuntimeException("No autorizado");

        var enrollments = enrollmentRepo.findByCourseId(courseId);
        var studentIds = enrollments.stream().map(Enrollment::getStudentId).toList();
        var students = studentIds.isEmpty() ? List.<User>of() : userRepo.findByIdIn(studentIds);

        var assignments = assignmentRepo.findByCourseIdOrderByDueAtAsc(courseId);
        var syllabi = syllabusRepo.findByCourseIdOrderByVersionDesc(courseId);
        var rubrics = rubricRepo.findByCourseIdOrderByVersionDesc(courseId);

        model.addAttribute("course", course);
        model.addAttribute("students", students);
        model.addAttribute("assignments", assignments);
        model.addAttribute("syllabi", syllabi);
        model.addAttribute("rubrics", rubrics);
        if (!model.containsAttribute("form")) model.addAttribute("form", new CreateAssignmentDTO());
        return "teacher/CourseDetail";
    }

    @PostMapping("/course/{courseId}/assignment")
    public String createAssignmentInCourse(@PathVariable String courseId,
                                           @AuthenticationPrincipal CustomUserDetails me,
                                           @Valid @ModelAttribute("form") CreateAssignmentDTO form,
                                           BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.form", br);
            ra.addFlashAttribute("form", form);
            return "redirect:/teacher/course/" + courseId;
        }
        var a = Assignment.builder()
                .id(UUID.randomUUID().toString())
                .courseId(courseId)
                .title(form.getTitle())
                .description(form.getDescription())
                .dueAt(form.getDueAt()!=null ? form.getDueAt().atZone(ZoneId.systemDefault()).toInstant() : null)
                .publishedAt(Instant.now())
                .createdBy(me.getUserId())
                .createdAt(Instant.now())
                .build();
        // opcional: set rubric y syllabus si tu entidad Assignment ya tiene esos campos en el modelo
        try { // si tu modelo tiene setters, úsalos
            var aClass = a.getClass();
            var setRubric = aClass.getMethod("setRubricId", String.class);
            setRubric.invoke(a, form.getRubricId());
            if (form.getSyllabusId()!=null && !form.getSyllabusId().isBlank()) {
                var setSyllabus = aClass.getMethod("setSyllabusId", String.class);
                setSyllabus.invoke(a, form.getSyllabusId());
            }
        } catch (Exception ignore) { /* si tu entidad aún no expone campos rubricId/syllabusId, no falla */ }

        assignmentRepo.save(a);
        ra.addFlashAttribute("success","Tarea creada");
        return "redirect:/teacher/course/" + courseId;
    }

    @GetMapping("/assignment/{assignmentId}")
    public String assignmentDetail(@PathVariable String assignmentId,
                                   @AuthenticationPrincipal CustomUserDetails me,
                                   Model model) {
        var assignment = assignmentRepo.findById(assignmentId).orElseThrow();
        var courseId = assignment.getCourseId();

        // seguridad: profe debe ser instructor del curso
        var isInstructor = courseInstructorRepo.findByUserId(me.getUserId())
                .stream().anyMatch(ci -> ci.getCourseId().equals(courseId));
        if (!isInstructor) throw new RuntimeException("No autorizado");

        var course = courseRepo.findById(courseId).orElseThrow();
        var enrollments = enrollmentRepo.findByCourseId(courseId);
        var studentIds = enrollments.stream().map(Enrollment::getStudentId).toList();
        var students = studentIds.isEmpty() ? List.<User>of() : userRepo.findByIdIn(studentIds);

        // Mapa: studentId -> Submission
        var subs = submissionRepo.findByAssignmentId(assignmentId);
        var subByStudent = subs.stream().collect(Collectors.toMap(Submission::getStudentId, s -> s));

        record Row(User student, String status, String nota) {}
        List<Row> rows = new ArrayList<>();
        for (var st : students) {
            var s = subByStudent.get(st.getId());
            String status = (s==null) ? "Sin enviar" : s.getStatus().name();
            String nota = "Por calificar";
            if (s != null && s.getFinalScore() != null && s.getFinalScore() > 0.0) {
                nota = String.valueOf(s.getFinalScore());
            }
            rows.add(new Row(st, status, nota));
        }

        model.addAttribute("course", course);
        model.addAttribute("assignment", assignment);
        model.addAttribute("rows", rows);
        return "teacher/AssignmentDetail";
    }

    @PostMapping("/upload-syllabus")
    public String uploadSyllabus(@AuthenticationPrincipal CustomUserDetails me,
                                 @Valid @ModelAttribute("syllabusForm") SyllabusUploadDTO form,
                                 BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.syllabusForm", br);
            ra.addFlashAttribute("syllabusForm", form);
            return "redirect:/teacher/portal";
        }
        // OJO: ya no usamos form.getVersion()
        teacherService.uploadSyllabusAutoVersion(form.getCourseId(), form.getTitle(),
                form.getFile(), me.getUserId());
        ra.addFlashAttribute("success","Temario subido");
        return "redirect:/teacher/course/" + form.getCourseId();
    }




    @PostMapping("/upload-rubric")
    public String uploadRubric(@AuthenticationPrincipal CustomUserDetails me,
                               @Valid @ModelAttribute("rubricForm") RubricUploadDTO form,
                               BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rubricForm", br);
            ra.addFlashAttribute("rubricForm", form);
            return "redirect:/teacher/portal";
        }
        teacherService.uploadRubric(form.getCourseId(), form.getName(), form.getVersion(),
                form.getFile(), me.getUserId());
        ra.addFlashAttribute("success","Rúbrica subida");
        return "redirect:/teacher/course/" + form.getCourseId();
    }

    //Para ver que esta fallando
    @ExceptionHandler(Exception.class)
    public String handleEx(Exception ex, RedirectAttributes ra) {
        ra.addFlashAttribute("error", "Error: " + ex.getMessage());
        return "redirect:/teacher/course/new";
    }

}
