package com.orbit.team.dto.response;

import com.orbit.team.entity.RsvpStatus;
import lombok.Data;

@Data
public class AttendeeResponse {
    private Long userId;
    private String fullName;
    private RsvpStatus status;
}
