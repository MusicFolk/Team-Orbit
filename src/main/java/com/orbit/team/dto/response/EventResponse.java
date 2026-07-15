package com.orbit.team.dto.response;

import com.orbit.team.entity.EventCategory;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class EventResponse {

    private Long eventId;
    private String title;
    private String description;
    private String location;
    private LocalDateTime eventDateTime;
    private Integer capacity;
    private EventCategory category;
    private String organizer;
}
