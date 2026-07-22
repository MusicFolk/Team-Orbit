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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RsvpService {

    private final RsvpRepository rsvpRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public AttendeeResponse createRsvp(Long userId, Long eventId, RsvpStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        if (rsvpRepository.findByUserAndEvent(user, event).isPresent()) {
            throw new RsvpConflictException("RSVP already exists for this event");
        }

        if (status == RsvpStatus.ATTENDING) {
            long attendingCount = rsvpRepository.countByEvent_IdAndStatus(eventId, RsvpStatus.ATTENDING);
            if (attendingCount >= event.getCapacity()) {
                throw new EventFullException("Event has reached its attendee capacity");
            }
        }

        RSVP rsvp = RSVP.builder()
                .user(user)
                .event(event)
                .status(status)
                .build();

        try {
            return toResponse(rsvpRepository.save(rsvp));
        } catch (DataIntegrityViolationException ex) {
            throw new RsvpConflictException("Could not create RSVP due to a data conflict", ex);
        }
    }

    public AttendeeResponse updateRsvp(Long userId, Long eventId, RsvpStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        RSVP rsvp = rsvpRepository.findByUserAndEvent(user, event)
                .orElseThrow(() -> new ResourceNotFoundException("No existing RSVP to update for this event"));

        if (status == RsvpStatus.ATTENDING && rsvp.getStatus() != RsvpStatus.ATTENDING) {
            long attendingCount = rsvpRepository.countByEvent_IdAndStatus(eventId, RsvpStatus.ATTENDING);
            if (attendingCount >= event.getCapacity()) {
                throw new EventFullException("Event has reached its attendee capacity");
            }
        }

        rsvp.setStatus(status);

        try {
            return toResponse(rsvpRepository.save(rsvp));
        } catch (DataIntegrityViolationException ex) {
            throw new RsvpConflictException("Could not update RSVP due to a data conflict", ex);
        }
    }

    public void cancelRsvp(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        rsvpRepository.findByUserAndEvent(user, event)
                .ifPresent(rsvpRepository::delete);
    }

    public List<AttendeeResponse> getRsvpsForEvent(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        if (!event.getOrganizer().getId().equals(userId)) {
            throw new UnauthorizedActionException("Only the organizer can view the attendee list");
        }

        return rsvpRepository.findByEvent_Id(eventId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private AttendeeResponse toResponse(RSVP rsvp) {
        AttendeeResponse response = new AttendeeResponse();
        response.setUserId(rsvp.getUser().getId());
        response.setFullName(rsvp.getUser().getFullName());
        response.setStatus(rsvp.getStatus());
        return response;
    }
}