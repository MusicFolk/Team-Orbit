package com.orbit.team.controller;

import com.orbit.team.service.EventService;
import com.orbit.team.service.RsvpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final EventService eventService;
    private final RsvpService rsvpService;

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

        // echo the filters back so the form stays populated after a search
        model.addAttribute("keyword", q);
        model.addAttribute("selectedCity", cityFilter);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedDate", date);

        return "dashboard";
    }
}
