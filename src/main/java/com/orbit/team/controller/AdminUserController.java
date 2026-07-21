package com.orbit.team.controller;

import com.orbit.team.dto.response.UserResponse;
import com.orbit.team.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Users")
public class AdminUserController {
    private final UserService userService;

    @Operation(summary = "Get all users as admin")
    @ApiResponse(responseCode = "200", description = "User list returned successfully")
    @ApiResponse(responseCode = "403", description = "Requires ADMIN role")
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @Operation(summary = "Activate or deactivate a user as admin")
    @ApiResponse(responseCode = "200", description = "User status updated successfully")
    @ApiResponse(responseCode = "403", description = "Requires ADMIN role")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PatchMapping("/{id}/status")
    public void disableUser(@PathVariable Long id, @RequestParam boolean status) {
        userService.setStatus(id, status);
    }


}
