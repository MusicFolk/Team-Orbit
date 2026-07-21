package com.orbit.team.controller.rest;

import com.orbit.team.dto.request.RegisterRequest;
import com.orbit.team.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/api/auth/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/api/auth/register")
    public String registerPage(Model model) {
        model.addAttribute("registerForm", new RegisterRequest());
        return "register";
    }

    @PostMapping("/api/auth/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterRequest registerForm, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            userService.registerUser(registerForm);
        } catch (IllegalArgumentException illegalArgumentException) {
            model.addAttribute("registrationError", illegalArgumentException.getMessage());
            return "register";
        }
        return "redirect:/api/auth/login";
    }
}
