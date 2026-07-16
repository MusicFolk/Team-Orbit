package com.orbit.team.controller;

import com.orbit.team.dto.request.RsvpRequest;
import com.orbit.team.dto.response.AttendeeResponse;
import com.orbit.team.security.UserSecurity;
import com.orbit.team.service.RsvpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class RsvpController {

    private final RsvpService rsvpService;

    @PostMapping("/{id}/rsvp")
    @ResponseStatus(HttpStatus.CREATED)
    public AttendeeResponse createRsvp(@PathVariable Long id,
                                       @Valid @RequestBody RsvpRequest request,
                                       @AuthenticationPrincipal UserSecurity userSecurity) {
        return rsvpService.createRsvp(userSecurity.getId(), id, request.getStatus());
    }

    @PutMapping("/{id}/rsvp")
    public AttendeeResponse updateRsvp(@PathVariable Long id,
                                       @Valid @RequestBody RsvpRequest request,
                                       @AuthenticationPrincipal UserSecurity userSecurity) {
        return rsvpService.updateRsvp(userSecurity.getId(), id, request.getStatus());
    }

    @DeleteMapping("/{id}/rsvp")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelRsvp(@PathVariable Long id,
                           @AuthenticationPrincipal UserSecurity userSecurity) {
        rsvpService.cancelRsvp(userSecurity.getId(), id);
    }
}