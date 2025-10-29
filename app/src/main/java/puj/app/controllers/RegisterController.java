package puj.app.controllers;

import puj.app.DTOs.RegisterDTO;
import puj.app.model.Role;
import puj.app.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/register")
public class RegisterController {

    private final RegistrationService registrationService;

    @GetMapping("/professor")
    public String professorForm(Model model) {
        if (!model.containsAttribute("form")) model.addAttribute("form", new RegisterDTO());
        return "auth/RegisterProfessor";
    }

    @PostMapping("/professor")
    public String registerProfessor(@Valid @ModelAttribute("form") RegisterDTO form,
                                    BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.form", br);
            ra.addFlashAttribute("form", form);
            return "redirect:/register/professor";
        }
        try {
            registrationService.register(form.getEmail(), form.getFullName(), form.getPassword(), Role.PROFESOR);
            ra.addFlashAttribute("success", "Profesor creado. Ya puedes iniciar sesión.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            ra.addFlashAttribute("form", form);
            return "redirect:/register/professor";
        }
    }

    @GetMapping("/student")
    public String studentForm(Model model) {
        if (!model.containsAttribute("form")) model.addAttribute("form", new RegisterDTO());
        return "auth/RegisterStudent";
    }

    @PostMapping("/student")
    public String registerStudent(@Valid @ModelAttribute("form") RegisterDTO form,
                                  BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.form", br);
            ra.addFlashAttribute("form", form);
            return "redirect:/register/student";
        }
        try {
            registrationService.register(form.getEmail(), form.getFullName(), form.getPassword(), Role.ALUMNO);
            ra.addFlashAttribute("success", "Alumno creado. Ya puedes iniciar sesión.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            ra.addFlashAttribute("form", form);
            return "redirect:/register/student";
        }
    }
}
