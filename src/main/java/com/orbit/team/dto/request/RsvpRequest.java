package com.orbit.team.dto.request;

import com.orbit.team.entity.RsvpStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RsvpRequest {
    @NotNull
    private RsvpStatus status;
}
