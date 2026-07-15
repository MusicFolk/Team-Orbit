package com.orbit.team.controller;

import com.orbit.team.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@EnableMethodSecurity
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final UserService userService;

    @GetMapping
    public String getAllUsers() {
        return null;
    }

    @PatchMapping("/{id}/status")
    public void disableUser(@PathVariable Long id) {}


}
