package com.orbit.team.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class EventResponse {

    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime eventDateTime;
    private Integer capacity;
    private Long categoryId;
    private String organizer;
}
