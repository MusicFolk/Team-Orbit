package com.orbit.team.controller.rest;

import com.orbit.team.dto.request.RsvpRequest;
import com.orbit.team.dto.response.AttendeeResponse;
import com.orbit.team.security.UserSecurity;
import com.orbit.team.service.RsvpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "RSVP")
public class RsvpController {

    private final RsvpService rsvpService;

    @Operation(summary = "Create RSVP for an event")
    @ApiResponse(responseCode = "201", description = "RSVP created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "404", description = "Event not found")
    @ApiResponse(responseCode = "409", description = "RSVP already exists, or event has reached capacity")
    @PostMapping("/{id}/rsvp")
    @ResponseStatus(HttpStatus.CREATED)
    public AttendeeResponse createRsvp(@PathVariable Long id,
                                       @Valid @RequestBody RsvpRequest request,
                                       @AuthenticationPrincipal UserSecurity userSecurity) {
        return rsvpService.createRsvp(userSecurity.getId(), id, request.getStatus());
    }

    @Operation(summary = "Update an existing RSVP status")
    @ApiResponse(responseCode = "200", description = "RSVP updated successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "404", description = "Event not found, or no existing RSVP to update")
    @ApiResponse(responseCode = "409", description = "Data conflict or event has reached max capacity when switching to attending")
    @PutMapping("/{id}/rsvp")
    public AttendeeResponse updateRsvp(@PathVariable Long id,
                                       @Valid @RequestBody RsvpRequest request,
                                       @AuthenticationPrincipal UserSecurity userSecurity) {
        return rsvpService.updateRsvp(userSecurity.getId(), id, request.getStatus());
    }

    @Operation(summary = "Cancel an RSVP for an event")
    @ApiResponse(responseCode = "204", description = "RSVP cancelled")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "404", description = "User not found or event not found")
    @DeleteMapping("/{id}/rsvp")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelRsvp(@PathVariable Long id,
                           @AuthenticationPrincipal UserSecurity userSecurity) {
        rsvpService.cancelRsvp(userSecurity.getId(), id);
    }
}