package puj.app.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {
    @GetMapping("/login")
    public String login() { return "auth/Login"; }

    @GetMapping("/")
    public String home() { return "redirect:/login"; }
}