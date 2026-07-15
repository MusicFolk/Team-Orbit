package com.orbit.team.repository;

import com.orbit.team.entity.Event;
import com.orbit.team.entity.EventCategory;
import com.orbit.team.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByOrganizer(User organizer);

    List<Event> findByCategory(EventCategory category);

    List<Event> findByLocationContainingIgnoreCase(String location);

    List<Event> findByEventDateTimeAfter(LocalDateTime dateTime);

    List<Event> findByEventDateTimeBefore(LocalDateTime dateTime);

    List<Event> findByEventDateTimeAfterOrderByEventDateTimeAsc(LocalDateTime dateTime);
}
