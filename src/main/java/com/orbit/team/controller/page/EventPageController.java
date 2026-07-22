package com.orbit.team.controller.page;

import com.orbit.team.dto.request.EventRequest;
import com.orbit.team.dto.response.AttendeeResponse;
import com.orbit.team.entity.Event;
import com.orbit.team.entity.EventCategory;
import com.orbit.team.exception.UnauthorizedActionException;
import com.orbit.team.security.UserSecurity;
import com.orbit.team.service.CommentService;
import com.orbit.team.service.EventService;
import com.orbit.team.service.RsvpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventPageController {

    private final EventService eventService;
    private final RsvpService rsvpService;
    private final CommentService commentService;

    @GetMapping
    public String dashboard(@RequestParam(required = false) String keyword,
                            @RequestParam(required = false) String city,
                            @RequestParam(required = false) EventCategory category,
                            @RequestParam(required = false)
                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,

                            @RequestParam(required = false)
                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,

                            Model model) {

        String q = blankToNull(keyword);
        String cityFilter = blankToNull(city);
        List<Event> all = eventService.getAllEvents();
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {

            List<String> cities = all.stream()
                    .map(Event::getLocation)
                    .distinct()
                    .sorted()
                    .toList();

            model.addAttribute("events", List.of());
            model.addAttribute("cities", cities);
            model.addAttribute("categories", EventCategory.values());

            model.addAttribute("keyword", q);
            model.addAttribute("selectedCity", cityFilter);
            model.addAttribute("selectedCategory", category);
            model.addAttribute("selectedFromDate", fromDate);
            model.addAttribute("selectedToDate", toDate);

            model.addAttribute("error", "From date must be before or equal to To date.");

            return "events";
        }
        List<Event> events = all.stream()
                .filter(e -> q == null || contains(e.getTitle(), q) || contains(e.getDescription(), q))
                .filter(e -> cityFilter == null || cityFilter.equalsIgnoreCase(e.getLocation()))
                .filter(e -> category == null || e.getCategory() == category)
                .filter(e -> {
                    LocalDate eventDate = e.getEventDateTime().toLocalDate();

                    if (fromDate != null && eventDate.isBefore(fromDate)) {
                        return false;
                    }

                    if (toDate != null && eventDate.isAfter(toDate)) {
                        return false;
                    }

                    return true;
                })
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
        model.addAttribute("selectedFromDate", fromDate);
        model.addAttribute("selectedToDate", toDate);

        return "events";
    }

    @GetMapping("/{id}")
    public String eventDetail(@PathVariable Long id,
                              @RequestParam(required = false) String rsvpError,
                              @AuthenticationPrincipal UserSecurity currentUser,
                              Model model) {

        Event event = eventService.getEventById(id);
        boolean isOrganizer = event.getOrganizer().getId().equals(currentUser.getId());

        model.addAttribute("event", event);
        model.addAttribute("isOrganizer", isOrganizer);
        model.addAttribute("rsvpError", rsvpError);

        if (isOrganizer) {
            List<AttendeeResponse> attendees =
                    rsvpService.getRsvpsForEvent(id, currentUser.getId());
            model.addAttribute("attendees", attendees);
            model.addAttribute("attendeeCount", (long) attendees.size());
        }

        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("comments", commentService.getCommentsForEvent(id));

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

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private static boolean contains(String haystack, String needle) {
        return haystack != null
                && haystack.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }
}