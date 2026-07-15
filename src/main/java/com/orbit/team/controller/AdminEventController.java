package com.orbit.team.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
@EnableMethodSecurity
@PreAuthorize("hasRole('ADMIN')")
public class AdminEventController {

    // private EventService eventService;

    @GetMapping
    public String getAllEvents() {
        return null;
    }

    @DeleteMapping("/{id}")
    public void deleteEvent(@PathVariable Long id) {}

/*    @PutMapping("/{id}")
    public void updateEvent(@PathVariable Long id) {}*/
}
