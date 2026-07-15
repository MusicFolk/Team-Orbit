package com.orbit.team.service;

import com.orbit.team.dto.request.EventRequest;
import com.orbit.team.entity.Category;
import com.orbit.team.entity.Event;
import com.orbit.team.entity.User;
import com.orbit.team.repository.CategoryRepository;
import com.orbit.team.repository.EventRepository;
import com.orbit.team.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public Event createEvent(EventRequest request, Long userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.getCategoryId()));

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .eventDateTime(request.getEventDateTime())
                .capacity(request.getCapacity())
                .organizer(currentUser)
                .category(category)
                .build();

        return eventRepository.save(event);
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));
    }

    public Event updateEvent(Long id, EventRequest request, Long userId) {
        Event event = getEventById(id);

        if (!event.getOrganizer().getId().equals(userId)) {
            throw new IllegalArgumentException("Only the organizer can edit this event");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.getCategoryId()));

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setEventDateTime(request.getEventDateTime());
        event.setCapacity(request.getCapacity());
        event.setCategory(category);

        return eventRepository.save(event);
    }

    public void deleteEvent(Long id, Long userId) {
        Event event = getEventById(id);

        if (!event.getOrganizer().getId().equals(userId)) {
            throw new IllegalArgumentException("Only the organizer can delete this event");
        }

        eventRepository.delete(event);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }
}
