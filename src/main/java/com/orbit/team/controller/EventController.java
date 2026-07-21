package com.orbit.team.controller;

import com.orbit.team.dto.request.EventRequest;
import com.orbit.team.dto.response.AttendeeResponse;
import com.orbit.team.dto.response.EventResponse;
import com.orbit.team.entity.Event;
import com.orbit.team.security.UserSecurity;
import com.orbit.team.service.EventService;
import com.orbit.team.service.RsvpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Events")
public class EventController {

    private final EventService eventService;
    private final RsvpService rsvpService;

    @Operation(summary = "Get all events")
    @ApiResponse(responseCode = "200", description = "List of events returned")
    @GetMapping
    public List<EventResponse> getAllEvents() {
        return eventService.getAllEvents().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Get a single event by ID")
    @ApiResponse(responseCode = "200", description = "Event found")
    @ApiResponse(responseCode = "404", description = "Event not found")
    @GetMapping("/{id}")
    public EventResponse getEventById(@PathVariable Long id) {
        return toResponse(eventService.getEventById(id));
    }

    @Operation(summary = "Create a new event")
    @ApiResponse(responseCode = "201", description = "Event created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse createEvent(@Valid @RequestBody EventRequest request,
                                     @AuthenticationPrincipal UserSecurity userSecurity) {
        Event event = eventService.createEvent(request, userSecurity.getId());
        return toResponse(event);
    }

    @Operation(summary = "Update an existing event")
    @ApiResponse(responseCode = "200", description = "Event updated successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "403", description = "Only the organizer can edit this event")
    @ApiResponse(responseCode = "404", description = "Event not found")
    @PutMapping("/{id}")
    public EventResponse updateEvent(@PathVariable Long id,
                                     @Valid @RequestBody EventRequest request,
                                     @AuthenticationPrincipal UserSecurity userSecurity) {
        Event event = eventService.updateEvent(id, request, userSecurity.getId());
        return toResponse(event);
    }

    @Operation(summary = "Delete an event")
    @ApiResponse(responseCode = "204", description = "Event deleted successfully")
    @ApiResponse(responseCode = "403", description = "Only the organizer can delete this event")
    @ApiResponse(responseCode = "404", description = "Event not found")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable Long id,
                            @AuthenticationPrincipal UserSecurity userSecurity) {
        eventService.deleteEvent(id, userSecurity.getId());
    }

    private EventResponse toResponse(Event event) {
        EventResponse response = new EventResponse();
        response.setEventId(event.getId());
        response.setTitle(event.getTitle());
        response.setDescription(event.getDescription());
        response.setLocation(event.getLocation());
        response.setEventDateTime(event.getEventDateTime());
        response.setCapacity(event.getCapacity());
        response.setCategory(event.getCategory());
        response.setOrganizer(event.getOrganizer().getUsername());
        return response;
    }

    @Operation(summary = "Get attendee list for an event")
    @ApiResponse(responseCode = "200", description = "Attendee list returned")
    @ApiResponse(responseCode = "403", description = "Only the organizer can view the attendee list")
    @ApiResponse(responseCode = "404", description = "Event not found")
    @GetMapping("/{id}/attendees")
    public List<AttendeeResponse> getAttendees(@PathVariable Long id, @AuthenticationPrincipal UserSecurity userSecurity) {
        return rsvpService.getRsvpsForEvent(id, userSecurity.getId());
    }
}
