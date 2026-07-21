package com.orbit.team.repository;

import com.orbit.team.entity.Event;
import com.orbit.team.entity.RSVP;
import com.orbit.team.entity.RsvpStatus;
import com.orbit.team.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RsvpRepository extends JpaRepository<RSVP, Long> {

    List<RSVP> findByEvent(Event event);

    List<RSVP> findByEvent_Id(Long eventId);

    List<RSVP> findByUser(User user);

    List<RSVP> findByUser_Id(Long userId);

    Optional<RSVP> findByUserAndEvent(User user, Event event);

    long countByEvent_IdAndStatus(Long eventId, RsvpStatus status);
}