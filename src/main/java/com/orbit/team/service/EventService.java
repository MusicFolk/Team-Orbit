package com.orbit.team.service;

import com.orbit.team.dto.request.EventRequest;
import com.orbit.team.entity.Event;
import com.orbit.team.entity.User;
import com.orbit.team.exception.ResourceNotFoundException;
import com.orbit.team.exception.UnauthorizedActionException;
import com.orbit.team.repository.CommentRepository;
import com.orbit.team.repository.EventRepository;
import com.orbit.team.repository.RsvpRepository;
import com.orbit.team.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RsvpRepository rsvpRepository;
    private final CommentRepository commentRepository;

    public Event createEvent(EventRequest request, Long userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .eventDateTime(request.getEventDateTime())
                .capacity(request.getCapacity())
                .organizer(currentUser)
                .category(request.getCategory())
                .build();

        return eventRepository.save(event);
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + id));
    }

    public Event updateEvent(Long id, EventRequest request, Long userId) {
        Event event = getEventById(id);

        if (!event.getOrganizer().getId().equals(userId)) {
            throw new UnauthorizedActionException("Only the organizer can edit this event");
        }

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setEventDateTime(request.getEventDateTime());
        event.setCapacity(request.getCapacity());
        event.setCategory(request.getCategory());

        return eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long id, Long userId) {
        Event event = getEventById(id);

        if (!event.getOrganizer().getId().equals(userId)) {
            throw new UnauthorizedActionException("Only the organizer can delete this event");
        }

        rsvpRepository.deleteAll(rsvpRepository.findByEvent(event));
        commentRepository.deleteAll(commentRepository.findByEventOrderByCreatedAtAsc(event));
        eventRepository.delete(event);
    }

    @Transactional
    public void deleteEventAsAdmin(Long id) {
        Event event = getEventById(id);

        rsvpRepository.deleteAll(rsvpRepository.findByEvent(event));
        commentRepository.deleteAll(commentRepository.findByEventOrderByCreatedAtAsc(event));
        eventRepository.delete(event);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }
}