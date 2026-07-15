package com.orbit.team.controller;

import com.orbit.team.dto.request.RsvpRequest;
import com.orbit.team.dto.response.AttendeeResponse;
import com.orbit.team.entity.RSVP;
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

    @PutMapping("/{id}/rsvp")
    public AttendeeResponse submitRsvp(@PathVariable Long id,
                                       @Valid @RequestBody RsvpRequest request,
                                       @AuthenticationPrincipal UserSecurity userSecurity) {
        RSVP rsvp = rsvpService.submitRsvp(userSecurity.getId(), id, request.getStatus());
        return toResponse(rsvp);
    }

    @DeleteMapping("/{id}/rsvp")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelRsvp(@PathVariable Long id,
                           @AuthenticationPrincipal UserSecurity userSecurity) {
        rsvpService.cancelRsvp(userSecurity.getId(), id);
    }

    private AttendeeResponse toResponse(RSVP rsvp) {
        AttendeeResponse response = new AttendeeResponse();
        response.setUserId(rsvp.getUser().getId());
        response.setFullName(rsvp.getUser().getFullName());
        response.setStatus(rsvp.getStatus());
        return response;
    }
}