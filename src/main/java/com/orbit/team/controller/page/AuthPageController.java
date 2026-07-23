package com.orbit.team.controller.page;

import com.orbit.team.dto.request.RegisterRequest;
import com.orbit.team.exception.DuplicateEmailException;
import com.orbit.team.exception.DuplicateUsernameException;
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
public class AuthPageController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerForm", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterRequest registerForm, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            userService.registerUser(registerForm);
        } catch (DuplicateUsernameException | DuplicateEmailException ex) {
            model.addAttribute("registrationError", ex.getMessage());
            return "register";
        }
        return "redirect:/login";
    }
}
