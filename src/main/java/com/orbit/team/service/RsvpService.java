package com.orbit.team.service;

import com.orbit.team.entity.Event;
import com.orbit.team.entity.RSVP;
import com.orbit.team.entity.RsvpStatus;
import com.orbit.team.entity.User;
import com.orbit.team.repository.EventRepository;
import com.orbit.team.repository.RsvpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RsvpService {

    private final RsvpRepository rsvpRepository;
    private final EventRepository eventRepository;

    public RSVP submitRsvp(User user, Long eventId, RsvpStatus status) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        return rsvpRepository.findByUserAndEvent(user, event)
                .map(existing -> {
                    existing.setStatus(status);
                    return rsvpRepository.save(existing);
                })
                .orElseGet(() -> {
                    RSVP rsvp = RSVP.builder()
                            .user(user)
                            .event(event)
                            .status(status)
                            .build();
                    return rsvpRepository.save(rsvp);
                });
    }

    public void cancelRsvp(User user, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        rsvpRepository.findByUserAndEvent(user, event)
                .ifPresent(rsvpRepository::delete);
    }

    public List<RSVP> getRsvpsForEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new IllegalArgumentException("Event not found: " + eventId);
        }
        return rsvpRepository.findByEvent_Id(eventId);
    }

    public List<RSVP> getRsvpsForUser(Long userId) {
        return rsvpRepository.findByUser_Id(userId);
    }
}