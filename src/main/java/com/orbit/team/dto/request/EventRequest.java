package com.orbit.team.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String description;

    @NotBlank
    private String location;

    @NotNull
    @Future
    private LocalDateTime eventDateTime;

    @NotNull
    @Positive
    private Integer capacity;

    @NotNull
    private Long categoryId;
}
