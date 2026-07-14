package com.orbit.team.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventRequest {
    @NotBlank
    @Size(max = 150)
    private String title;
    @NotBlank
    private String description;

    @NotBlank
    @Size(max = 150)
    private String location;

    @NotNull
    @Future
    private LocalDateTime eventDateTime;

    @Min(1)
    private Integer capacity;

    @NotNull
    private Long categoryId;
}
