package com.orbit.team.controller;

import com.orbit.team.dto.request.EventRequest;
import com.orbit.team.entity.Event;
import com.orbit.team.entity.EventCategory;
import com.orbit.team.exception.UnauthorizedActionException;
import com.orbit.team.security.UserSecurity;
import com.orbit.team.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventPageController {

    private final EventService eventService;

    @GetMapping
    public String dashboard(Model model) {

        model.addAttribute("events", eventService.getAllEvents());
        model.addAttribute("categories", EventCategory.values());

        return "events";
    }

    @GetMapping("/{id}")
    public String eventDetail(@PathVariable Long id, @AuthenticationPrincipal UserSecurity currentUser, Model model) {

        Event event = eventService.getEventById(id);

        model.addAttribute("event", event);
        model.addAttribute("isOrganizer", event.getOrganizer().getId().equals(currentUser.getId()));

        return "event_detail";
    }

    @GetMapping("/new")
    public String newEventForm(Model model) {

        model.addAttribute("eventForm", new EventRequest());
        model.addAttribute("isEdit", false);
        model.addAttribute("categories", EventCategory.values());

        return "event_form";
    }

    @GetMapping("/{id}/edit")
    public String editEventForm(@PathVariable Long id, @AuthenticationPrincipal UserSecurity currentUser, Model model) {

        Event event = eventService.getEventById(id);

        if (!event.getOrganizer()
                .getId()
                .equals(currentUser.getId())) {
            throw new UnauthorizedActionException("Only the organizer can edit this event");
        }

        model.addAttribute("eventForm", toRequest(event));
        model.addAttribute("eventId", id);
        model.addAttribute("isEdit", true);
        model.addAttribute("categories", EventCategory.values());

        return "event_form";
    }

    @PostMapping
    public String createEvent(@Valid @ModelAttribute("eventForm") EventRequest eventForm, BindingResult bindingResult,
                              @AuthenticationPrincipal UserSecurity currentUser, Model model) {

        if (bindingResult.hasErrors()) {

            model.addAttribute("isEdit", false);
            model.addAttribute("categories", EventCategory.values());

            return "event_form";
        }

        Event created = eventService.createEvent(eventForm, currentUser.getId());

        return "redirect:/events/" + created.getId();
    }

    @PutMapping("/{id}")
    public String updateEvent(@PathVariable Long id, @Valid @ModelAttribute("eventForm") EventRequest eventForm,
                              BindingResult bindingResult, @AuthenticationPrincipal UserSecurity currentUser, Model model) {

        if (bindingResult.hasErrors()) {

            model.addAttribute("eventId", id);
            model.addAttribute("isEdit", true);
            model.addAttribute("categories", EventCategory.values());

            return "event_form";
        }

        eventService.updateEvent(id, eventForm, currentUser.getId());
        return "redirect:/events/" + id;
    }

    @DeleteMapping("/{id}")
    public String deleteEvent(@PathVariable Long id, @AuthenticationPrincipal UserSecurity currentUser) {

        eventService.deleteEvent(id, currentUser.getId());
        return "redirect:/events";
    }

    private static EventRequest toRequest(Event event) {

        EventRequest request = new EventRequest();

        request.setTitle(event.getTitle());
        request.setDescription(event.getDescription());
        request.setLocation(event.getLocation());
        request.setCategory(event.getCategory());
        request.setCapacity(event.getCapacity());
        request.setEventDateTime(event.getEventDateTime());

        return request;
    }
}