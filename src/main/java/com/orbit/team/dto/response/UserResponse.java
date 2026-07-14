package com.orbit.team.dto.response;

import lombok.Data;

@Data
public class UserResponse {
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private boolean active;
}
