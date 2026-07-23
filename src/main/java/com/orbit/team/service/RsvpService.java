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

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Cannot create RSVP. User {} not found", userId);
                    return new ResourceNotFoundException("User not found: " + userId);
                });
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Cannot create RSVP. Event {} not found", eventId);
                    return new ResourceNotFoundException("Event not found: " + eventId);
                });

        if (rsvpRepository.findByUserAndEvent(user, event).isPresent()) {
            log.warn("User {} already has an RSVP for event {}", userId, eventId);
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
            log.warn("RSVP creation failed due to data conflict for user {} and event {}", userId, eventId);
            throw new RsvpConflictException("Could not create RSVP due to a data conflict", ex);
        }
    }

    public AttendeeResponse updateRsvp(Long userId, Long eventId, RsvpStatus status) {
        log.info("User {} updating RSVP for event {} to {}", userId, eventId, status);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Cannot update RSVP. User {} not found", userId);
                    return new ResourceNotFoundException("User not found: " + userId);
                });
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Cannot update RSVP. Event {} not found", eventId);
                    return new ResourceNotFoundException("Event not found: " + eventId);
                });

        RSVP rsvp = rsvpRepository.findByUserAndEvent(user, event)
                .orElseThrow(() -> {
                    log.warn("No RSVP found for user {} and event {}", userId, eventId);
                    return new ResourceNotFoundException("No existing RSVP to update for this event");
                });

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
            log.warn("RSVP update failed due to data conflict for user {} and event {}", userId, eventId);
            throw new RsvpConflictException("Could not update RSVP due to a data conflict", ex);
        }
    }

    public void cancelRsvp(Long userId, Long eventId) {
        log.info("User {} cancelling RSVP for event {}", userId, eventId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Cannot cancel RSVP. User {} not found", userId);
                    return new ResourceNotFoundException("User not found: " + userId);
                });
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Cannot cancel RSVP. Event {} not found", eventId);
                    return new ResourceNotFoundException("Event not found: "+ eventId);
                });

        rsvpRepository.findByUserAndEvent(user, event)
                .ifPresent(rsvpRepository::delete);
    }

    public List<AttendeeResponse> getRsvpsForEvent(Long eventId, Long userId) {
        log.info("User {} viewing attendees for event {}", userId, eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Cannot get attendees. Event {} not found", eventId);
                    return new ResourceNotFoundException("Event not found: " + eventId);
                });

        if (!event.getOrganizer().getId().equals(userId)) {
            log.warn("User {} attempted to view attendees of event {} without permission", userId, eventId);
            throw new UnauthorizedActionException("Only the organizer can view the attendee list");
        }

        return rsvpRepository.findByEvent_Id(eventId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<AttendeeResponse> getRsvpsForUser(Long userId) {
        log.info("Fetching RSVPs for user {}", userId);

        return rsvpRepository.findByUser_Id(userId).stream()
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