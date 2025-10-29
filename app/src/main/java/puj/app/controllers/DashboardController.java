package puj.app.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import puj.app.security.CustomUserDetails;

@Controller
public class DashboardController {
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails me, Model model) {
        model.addAttribute("me", me);
        return "Dashboard";
    }
}
