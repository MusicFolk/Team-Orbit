package com.orbit.team.dto.request;

import com.orbit.team.entity.RsvpStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RsvpRequest {
    @NotBlank
    private RsvpStatus status;
    @NotBlank
    private LocalDateTime createdAt;

}
