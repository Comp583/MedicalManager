package com.medicalmanager.medical.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        // This controller will redirect based on role
        return "forward:/role-redirect";
    }

    @GetMapping("/role-redirect")
    public String roleRedirect(jakarta.servlet.http.HttpServletRequest request) {
        if (request.isUserInRole("ADMIN")) {
            return "redirect:/admin/landing";
        } else if (request.isUserInRole("DOCTOR")) {
            return "redirect:/doctor/landing";
        } else if (request.isUserInRole("PATIENT")) {
            return "redirect:/patient/landing";
        }
        return "redirect:/login";
    }
}
