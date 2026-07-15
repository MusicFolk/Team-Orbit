package com.orbit.team.service;

import com.orbit.team.dto.request.EventRequest;
import com.orbit.team.entity.Category;
import com.orbit.team.entity.Event;
import com.orbit.team.entity.User;
import com.orbit.team.repository.CategoryRepository;
import com.orbit.team.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public Event createEvent(EventRequest request, User currentUser) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

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

    @Override
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));
    }

    @Override
    public Event updateEvent(Long id, EventRequest request, User currentUser) {
        Event event = getEventById(id);

        if (!event.getOrganizer().equals(currentUser)) {
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

    @Override
    public void deleteEvent(Long id, User currentUser) {
        Event event = getEventById(id);

        if (!event.getOrganizer().equals(currentUser)) {
            throw new IllegalArgumentException("Only the organizer can delete this event");
        }

        eventRepository.delete(event);
    }

    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }
}
