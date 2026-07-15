package com.orbit.team.controller;

import com.orbit.team.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private UserService userService;

    @GetMapping
    public String getAllUsers() {
        return null;
    }

    @PatchMapping("/{id}/status")
    public void disableUser(@PathVariable Long id) {}

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {}

}
