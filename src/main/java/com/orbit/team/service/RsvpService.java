package com.orbit.team.service;

import com.orbit.team.dto.response.AttendeeResponse;
import com.orbit.team.entity.Event;
import com.orbit.team.entity.RSVP;
import com.orbit.team.entity.RsvpStatus;
import com.orbit.team.entity.User;
import com.orbit.team.exception.EventFullException;
import com.orbit.team.exception.ResourceNotFoundException;
import com.orbit.team.exception.RsvpConflictException;
import com.orbit.team.exception.UnauthorizedActionException;
import com.orbit.team.repository.EventRepository;
import com.orbit.team.repository.RsvpRepository;
import com.orbit.team.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RsvpService {

    private final RsvpRepository rsvpRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public AttendeeResponse createRsvp(Long userId, Long eventId, RsvpStatus status) {
        log.info("User {} creating RSVP for event {} with status {}", userId, eventId, status);

        User user = findUserOrThrow(userId, "create RSVP");
        Event event = findEventOrThrow(eventId, "create RSVP");

        if (rsvpRepository.findByUserAndEvent(user, event).isPresent()) {
            log.warn("User {} already has an RSVP for event {}", userId, eventId);
            throw new RsvpConflictException("RSVP already exists for this event");
        }

        if (status == RsvpStatus.ATTENDING) {
            ensureCapacityAvailable(event);
        }

        RSVP rsvp = RSVP.builder()
                .user(user)
                .event(event)
                .status(status)
                .build();

        try {
            return toResponse(rsvpRepository.save(rsvp));
        } catch (DataIntegrityViolationException ex) {
            log.warn("RSVP creation failed due to data conflict for user {} and event {}", userId, eventId);
            throw new RsvpConflictException("Could not create RSVP due to a data conflict", ex);
        }
    }

    public AttendeeResponse updateRsvp(Long userId, Long eventId, RsvpStatus status) {
        log.info("User {} updating RSVP for event {} to {}", userId, eventId, status);

        User user = findUserOrThrow(userId, "update RSVP");
        Event event = findEventOrThrow(eventId, "update RSVP");

        RSVP rsvp = rsvpRepository.findByUserAndEvent(user, event)
                .orElseThrow(() -> {
                    log.warn("No RSVP found for user {} and event {}", userId, eventId);
                    return new ResourceNotFoundException("No existing RSVP to update for this event");
                });

        if (status == RsvpStatus.ATTENDING && rsvp.getStatus() != RsvpStatus.ATTENDING) {
            ensureCapacityAvailable(event);
        }

        rsvp.setStatus(status);

        try {
            return toResponse(rsvpRepository.save(rsvp));
        } catch (DataIntegrityViolationException ex) {
            log.warn("RSVP update failed due to data conflict for user {} and event {}", userId, eventId);
            throw new RsvpConflictException("Could not update RSVP due to a data conflict", ex);
        }
    }

    public void cancelRsvp(Long userId, Long eventId) {
        log.info("User {} cancelling RSVP for event {}", userId, eventId);

        User user = findUserOrThrow(userId, "cancel RSVP");
        Event event = findEventOrThrow(eventId, "cancel RSVP");

        rsvpRepository.findByUserAndEvent(user, event)
                .ifPresent(rsvpRepository::delete);
    }

    public List<AttendeeResponse> getRsvpsForEvent(Long eventId, Long userId) {
        log.info("User {} viewing attendees for event {}", userId, eventId);

        Event event = findEventOrThrow(eventId, "get attendees");

        if (!event.isOrganizedBy(userId)) {
            log.warn("User {} attempted to view attendees of event {} without permission", userId, eventId);
            throw new UnauthorizedActionException("Only the organizer can view the attendee list");
        }

        return rsvpRepository.findByEvent_Id(eventId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private User findUserOrThrow(Long userId, String action) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Cannot {}. User {} not found", action, userId);
                    return new ResourceNotFoundException("User not found: " + userId);
                });
    }

    private Event findEventOrThrow(Long eventId, String action) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Cannot {}. Event {} not found", action, eventId);
                    return new ResourceNotFoundException("Event not found: " + eventId);
                });
    }

    private void ensureCapacityAvailable(Event event) {
        long attendingCount = rsvpRepository.countByEvent_IdAndStatus(event.getId(), RsvpStatus.ATTENDING);
        if (attendingCount >= event.getCapacity()) {
            throw new EventFullException("Event has reached its attendee capacity");
        }
    }

    private AttendeeResponse toResponse(RSVP rsvp) {
        AttendeeResponse response = new AttendeeResponse();
        response.setUserId(rsvp.getUser().getId());
        response.setFullName(rsvp.getUser().getFullName());
        response.setStatus(rsvp.getStatus());
        return response;
    }
}