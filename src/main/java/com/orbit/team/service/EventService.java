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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RsvpRepository rsvpRepository;
    private final CommentRepository commentRepository;

    public Event createEvent(EventRequest request, Long userId) {
        log.info("User {} creating event '{}'", userId, request.getTitle());

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Cannot create event. User {} not found", userId);
                    return new ResourceNotFoundException("User not found: " + userId);
                });

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .eventDateTime(request.getEventDateTime())
                .capacity(request.getCapacity())
                .organizer(currentUser)
                .category(request.getCategory())
                .build();

        Event savedEvent = eventRepository.save(event);
        log.info("Event {} created successfully by user {}", savedEvent.getId(), userId);

        return savedEvent;
    }

    public Event getEventById(Long id) {
        log.info("Fetching event {}", id);

        return eventRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Event {} not found", id);
                    return new ResourceNotFoundException("Event not found: " + id);
                });
    }

    public Event updateEvent(Long id, EventRequest request, Long userId) {
        log.info("User {} updating event {}", userId, id);
        Event event = getEventById(id);

        if (!event.getOrganizer().getId().equals(userId)) {
            log.warn("User {} attempted to update event {} without permission", userId, id);
            throw new UnauthorizedActionException("Only the organizer can edit this event");
        }

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setEventDateTime(request.getEventDateTime());
        event.setCapacity(request.getCapacity());
        event.setCategory(request.getCategory());

        Event updatedEvent = eventRepository.save(event);
        log.info("Event {} updated successfully", id);

        return updatedEvent;
    }

    @Transactional
    public void deleteEvent(Long id, Long userId) {
        log.info("User {} deleting event {}", userId, id);

        Event event = getEventById(id);

        if (!event.getOrganizer().getId().equals(userId)) {
            log.warn("User {} attempted to delete event {} without permission", userId, id);
            throw new UnauthorizedActionException("Only the organizer can delete this event");
        }

        rsvpRepository.deleteAll(rsvpRepository.findByEvent(event));
        commentRepository.deleteAll(commentRepository.findByEventOrderByCreatedAtAsc(event));
        eventRepository.delete(event);

        log.info("Event {} deleted successfully by user {}", id, userId);
    }

    @Transactional
    public void deleteEventAsAdmin(Long id) {
        log.info("Admin deleting event {}", id);

        Event event = getEventById(id);

        rsvpRepository.deleteAll(rsvpRepository.findByEvent(event));
        commentRepository.deleteAll(commentRepository.findByEventOrderByCreatedAtAsc(event));
        eventRepository.delete(event);

        log.info("Event {} deleted successfully by admin", id);
    }

    public List<Event> getAllEvents() {
        log.info("Fetching all events");
        return eventRepository.findAll();
    }
}