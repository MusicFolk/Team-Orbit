package com.orbit.team.controller;

import com.orbit.team.dto.request.EventRequest;
import com.orbit.team.dto.response.AttendeeResponse;
import com.orbit.team.entity.Event;
import com.orbit.team.entity.EventCategory;
import com.orbit.team.entity.RsvpStatus;
import com.orbit.team.exception.UnauthorizedActionException;
import com.orbit.team.security.UserSecurity;
import com.orbit.team.service.CommentService;
import com.orbit.team.service.EventService;
import com.orbit.team.service.RsvpService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import com.orbit.team.dto.request.CommentRequest;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final EventService eventService;
    private final RsvpService rsvpService;
    private final CommentService commentService;

    @GetMapping("/home")
    public String home() {
        return "redirect:/events";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/events";
    }

    @GetMapping("/events")
    public String dashboard(@RequestParam(required = false) String keyword,
                            @RequestParam(required = false) String city,
                            @RequestParam(required = false) EventCategory category,
                            @RequestParam(required = false)
                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                            Model model) {

        String q = blankToNull(keyword);
        String cityFilter = blankToNull(city);
        List<Event> all = eventService.getAllEvents();

        List<Event> events = all.stream()
                .filter(e -> q == null || contains(e.getTitle(), q) || contains(e.getDescription(), q))
                .filter(e -> cityFilter == null || cityFilter.equalsIgnoreCase(e.getLocation()))
                .filter(e -> category == null || e.getCategory() == category)
                .filter(e -> date == null || e.getEventDateTime().toLocalDate().equals(date))
                .sorted(Comparator.comparing(Event::getEventDateTime))
                .toList();

        List<String> cities = all.stream()
                .map(Event::getLocation)
                .distinct()
                .sorted()
                .toList();

        model.addAttribute("events", events);
        model.addAttribute("cities", cities);
        model.addAttribute("categories", EventCategory.values());

        model.addAttribute("keyword", q);
        model.addAttribute("selectedCity", cityFilter);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedDate", date);

        return "events";
    }
    @GetMapping("/events/{id}")
    public String eventDetail(@PathVariable Long id,
                              @AuthenticationPrincipal UserSecurity currentUser,
                              Model model) {

        Event event = eventService.getEventById(id);
        List<AttendeeResponse> attendees = rsvpService.getRsvpsForEvent(id);

        model.addAttribute("event", event);
        model.addAttribute("isOrganizer", event.getOrganizer().getId().equals(currentUser.getId()));
        model.addAttribute("attendees", attendees);
        model.addAttribute("attendeeCount",
                attendees.stream().filter(a -> a.getStatus() == RsvpStatus.ATTENDING).count());

        attendees.stream()
                .filter(a -> a.getUserId().equals(currentUser.getId()))
                .findFirst()
                .ifPresent(a -> model.addAttribute("currentRsvpStatus", a.getStatus()));

        model.addAttribute("comments", commentService.getCommentsForEvent(id));

        return "event_detail";
    }

    @GetMapping("/events/new")
    public String newEventForm(Model model) {
        model.addAttribute("eventForm", new EventRequest());
        model.addAttribute("isEdit", false);
        model.addAttribute("categories", EventCategory.values());
        return "event_form";
    }

    @GetMapping("/events/{id}/edit")
    public String editEventForm(@PathVariable Long id,
                                @AuthenticationPrincipal UserSecurity currentUser,
                                Model model) {

        Event event = eventService.getEventById(id);
        if (!event.getOrganizer().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("Only the organizer can edit this event");
        }

        model.addAttribute("eventForm", toRequest(event));
        model.addAttribute("eventId", id);
        model.addAttribute("isEdit", true);
        model.addAttribute("categories", EventCategory.values());
        return "event_form";
    }

    @PostMapping("/events")
    public String createEvent(@Valid @ModelAttribute("eventForm") EventRequest eventForm,
                              BindingResult bindingResult,
                              @AuthenticationPrincipal UserSecurity currentUser,
                              Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("categories", EventCategory.values());
            return "event_form";
        }

        Event created = eventService.createEvent(eventForm, currentUser.getId());
        return "redirect:/events/" + created.getId();
    }

    @PutMapping("/events/{id}")
    public String updateEvent(@PathVariable Long id,
                              @Valid @ModelAttribute("eventForm") EventRequest eventForm,
                              BindingResult bindingResult,
                              @AuthenticationPrincipal UserSecurity currentUser,
                              Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("eventId", id);
            model.addAttribute("isEdit", true);
            model.addAttribute("categories", EventCategory.values());
            return "event_form";
        }

        eventService.updateEvent(id, eventForm, currentUser.getId());
        return "redirect:/events/" + id;
    }

    @DeleteMapping("/events/{id}")
    public String deleteEvent(@PathVariable Long id,
                              @AuthenticationPrincipal UserSecurity currentUser) {
        eventService.deleteEvent(id, currentUser.getId());
        return "redirect:/events";
    }

    @PostMapping("/events/{id}/rsvp")
    public String submitRsvp(@PathVariable Long id,
                             @RequestParam RsvpStatus status,
                             @AuthenticationPrincipal UserSecurity currentUser) {

        boolean alreadyRsvpd = rsvpService.getRsvpsForEvent(id).stream()
                .anyMatch(a -> a.getUserId().equals(currentUser.getId()));

        if (alreadyRsvpd) {
            rsvpService.updateRsvp(currentUser.getId(), id, status);
        } else {
            rsvpService.createRsvp(currentUser.getId(), id, status);
        }
        return "redirect:/events/" + id;
    }

    @PostMapping("/events/{id}/comments")
    public String addComment(@PathVariable Long id,
                             @RequestParam String content,
                             @AuthenticationPrincipal UserSecurity currentUser) {

        if (content != null && !content.isBlank()) {
            CommentRequest request = new CommentRequest();
            request.setContent(content);
            commentService.addComment(id, currentUser.getId(), request);
        }
        return "redirect:/events/" + id;
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

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private static boolean contains(String haystack, String needle) {
        return haystack != null
                && haystack.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }
}