package com.orbit.team.controller.rest;

import com.orbit.team.dto.response.UserResponse;
import com.orbit.team.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final UserService userService;

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @PatchMapping("/{id}/status")
    public void disableUser(@PathVariable Long id, @RequestParam boolean status) {
        userService.setStatus(id, status);
    }


}
