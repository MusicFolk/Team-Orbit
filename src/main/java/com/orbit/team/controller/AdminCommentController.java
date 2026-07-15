package com.orbit.team.controller;

import com.orbit.team.dto.response.CommentResponse;
import com.orbit.team.entity.Comment;
import com.orbit.team.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCommentController {

    private CommentRepository commentRepository;

    @GetMapping("/{eventId}")
    public List<CommentResponse> getAllComments(@PathVariable Long eventId) {

        return commentRepository.findByEvent_IdOrderByCreatedAtAsc(eventId).stream()
                .map(this::toResponse)
                .toList();
    }

    @DeleteMapping("/{id}")
    public void deleteComment(@PathVariable Long id) {}

}
