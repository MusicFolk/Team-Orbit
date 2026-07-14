package com.orbit.team.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponse {
    private Long commentId;
    private String text;
    private Long userId;
    private LocalDateTime createdAt;
}
