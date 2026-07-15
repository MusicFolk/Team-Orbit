package com.orbit.team.service;

import com.orbit.team.dto.request.CommentRequest;
import com.orbit.team.dto.response.CommentResponse;

import java.util.List;

public interface CommentService {

    CommentResponse addComment(Long eventId, Long userId, CommentRequest request);

    List<CommentResponse> getCommentsForEvent(Long eventId);

    void deleteComment(Long commentId, Long userId);
}