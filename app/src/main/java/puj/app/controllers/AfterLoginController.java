package puj.app.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AfterLoginController {

    @GetMapping("/after-login")
    public String afterLogin(Authentication auth) {
        boolean isProf = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_PROFESOR"));
        if (isProf) return "redirect:/teacher/portal";

        boolean isAlumno = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ALUMNO"));
        if (isAlumno) return "redirect:/student/portal";

        return "redirect:/dashboard";
    }
}