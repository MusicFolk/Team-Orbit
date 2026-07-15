package com.orbit.team.repository;

import com.orbit.team.entity.Event;
import com.orbit.team.entity.RSVP;
import com.orbit.team.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RsvpRepository extends JpaRepository<RSVP, Long> {

    List<RSVP> findByEvent(Event event);

    List<RSVP> findByUser(User user);

    Optional<RSVP> findByUserAndEvent(User user, Event event);
}