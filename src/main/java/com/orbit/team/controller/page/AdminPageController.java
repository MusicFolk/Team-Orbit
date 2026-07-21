package com.orbit.team.controller.page;

import com.orbit.team.entity.Comment;
import com.orbit.team.entity.Event;
import com.orbit.team.entity.User;
import com.orbit.team.service.AdminService;
import com.orbit.team.service.EventService;
import com.orbit.team.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminPageController {

    private final AdminService adminService;
    private final EventService eventService;
    private final UserService userService;

    @GetMapping("/users")
    public String users(@RequestParam(required = false) String search, Model model) {
        String q = blankToNull(search);
        model.addAttribute("users", adminService.getAllUsers().stream()
                .filter(u -> q == null
                        || contains(u.getFullName(), q)
                        || contains(u.getUsername(), q)
                        || contains(u.getEmail(), q))
                .toList());
        return "admin_users";
    }

    @PatchMapping("/users/{id}/status")
    public String toggleUserStatus(@PathVariable Long id) {
        boolean active = userService.getById(id).isActive();
        userService.setStatus(id, !active);
        return "redirect:/admin/users";
    }

    @GetMapping("/events")
    public String events(@RequestParam(required = false) String search, Model model) {
        String q = blankToNull(search);
        model.addAttribute("events", adminService.getAllEvents().stream()
                .filter(e -> q == null
                        || contains(e.getTitle(), q)
                        || contains(e.getOrganizer().getFullName(), q))
                .toList());
        return "admin_events";
    }

    @DeleteMapping("/events/{id}")
    public String deleteEvent(@PathVariable Long id) {
        eventService.deleteEventAsAdmin(id);
        return "redirect:/admin/events";
    }

    @GetMapping("/comments")
    public String comments(@RequestParam(required = false) String search,
                           @RequestParam(required = false) Long userId,
                           @RequestParam(required = false) Long eventId,
                           Model model) {
        String q = blankToNull(search);
        List<Comment> all = adminService.getAllComments();

        List<Comment> comments = all.stream()
                .filter(c -> q == null
                        || contains(c.getContent(), q)
                        || contains(c.getUser().getFullName(), q)
                        || contains(c.getEvent().getTitle(), q))
                .filter(c -> userId == null || c.getUser().getId().equals(userId))
                .filter(c -> eventId == null || c.getEvent().getId().equals(eventId))
                .toList();

        model.addAttribute("comments", comments);
        model.addAttribute("filterUsers", distinctUsers(all));
        model.addAttribute("filterEvents", distinctEvents(all));
        model.addAttribute("selectedUserId", userId);
        model.addAttribute("selectedEventId", eventId);
        return "admin_comments";
    }

    @DeleteMapping("/comments/{id}")
    public String deleteComment(@PathVariable Long id) {
        adminService.deleteComment(id);
        return "redirect:/admin/comments";
    }

    private static String blankToNull(String v) {
        return (v == null || v.isBlank()) ? null : v;
    }

    private static boolean contains(String haystack, String needle) {
        return haystack != null
                && haystack.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }

    private static List<User> distinctUsers(List<Comment> comments) {
        return comments.stream()
                .map(Comment::getUser)
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a, LinkedHashMap::new))
                .values().stream()
                .sorted(Comparator.comparing(User::getFullName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    private static List<Event> distinctEvents(List<Comment> comments) {
        return comments.stream()
                .map(Comment::getEvent)
                .collect(Collectors.toMap(Event::getId, e -> e, (a, b) -> a, LinkedHashMap::new))
                .values().stream()
                .sorted(Comparator.comparing(Event::getTitle, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }
}
