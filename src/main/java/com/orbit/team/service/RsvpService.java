package com.orbit.team.service;

import com.orbit.team.entity.RSVP;
import com.orbit.team.entity.RsvpStatus;
import com.orbit.team.entity.User;

import java.util.List;

public interface RsvpService {

    RSVP submitRsvp(User user, Long eventId, RsvpStatus status);

    void cancelRsvp(User user, Long eventId);

    List<RSVP> getRsvpsForEvent(Long eventId);

    List<RSVP> getRsvpsForUser(User user);
}