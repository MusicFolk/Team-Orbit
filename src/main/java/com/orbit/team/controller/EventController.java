package com.orbit.team.controller;

import com.orbit.team.dto.request.EventRequest;
import com.orbit.team.dto.response.AttendeeResponse;
import com.orbit.team.dto.response.EventResponse;
import com.orbit.team.entity.Event;
import com.orbit.team.security.UserSecurity;
import com.orbit.team.service.EventService;
import com.orbit.team.service.RsvpService;
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
public class EventController {

    private final EventService eventService;
    private final RsvpService rsvpService;

    @GetMapping
    public List<EventResponse> getAllEvents() {
        return eventService.getAllEvents().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public EventResponse getEventById(@PathVariable Long id) {
        return toResponse(eventService.getEventById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse createEvent(@Valid @RequestBody EventRequest request,
                                     @AuthenticationPrincipal UserSecurity userSecurity) {
        Event event = eventService.createEvent(request, userSecurity.getId());
        return toResponse(event);
    }

    @PutMapping("/{id}")
    public EventResponse updateEvent(@PathVariable Long id,
                                     @Valid @RequestBody EventRequest request,
                                     @AuthenticationPrincipal UserSecurity userSecurity) {
        Event event = eventService.updateEvent(id, request, userSecurity.getId());
        return toResponse(event);
    }

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

    @GetMapping("/{id}/attendees")
    public List<AttendeeResponse> getAttendees(@PathVariable Long id, @AuthenticationPrincipal UserSecurity userSecurity) {
        return rsvpService.getRsvpsForEvent(id, userSecurity.getId());
    }
}
