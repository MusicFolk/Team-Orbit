package com.orbit.team.dto.request;

import com.orbit.team.entity.RsvpStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class RsvpRequest {
    @NotNull
    private RsvpStatus status;
}
