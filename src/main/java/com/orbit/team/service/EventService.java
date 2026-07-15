package com.orbit.team.service;

import com.orbit.team.dto.request.EventRequest;
import com.orbit.team.entity.Event;
import com.orbit.team.entity.User;

import java.util.List;

public interface EventService {

    Event createEvent(EventRequest request, User currentUser);

    Event getEventById(Long id);

    Event updateEvent(Long id, EventRequest request, User currentUser);

    void deleteEvent(Long id, User currentUser);

    List<Event> getAllEvents();
}
