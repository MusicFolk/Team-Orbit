package com.orbit.team.controller.page;

import com.orbit.team.entity.RsvpStatus;
import com.orbit.team.exception.RsvpConflictException;
import com.orbit.team.security.UserSecurity;
import com.orbit.team.service.RsvpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class RsvpPageController {

    private final RsvpService rsvpService;

    @PostMapping("/events/{id}/rsvp")
    public String submitRsvp(@PathVariable Long id,
                             @RequestParam RsvpStatus status,
                             @AuthenticationPrincipal UserSecurity currentUser) {

        try {
            rsvpService.createRsvp(currentUser.getId(), id, status);
        } catch (RsvpConflictException ex) {
            rsvpService.updateRsvp(currentUser.getId(), id, status);
        }
        return "redirect:/events/" + id;
    }
}