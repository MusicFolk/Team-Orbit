package com.orbit.team.controller.rest;

import com.orbit.team.dto.response.CommentResponse;
import com.orbit.team.security.UserSecurity;
import com.orbit.team.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping("/{eventId}")
    public List<CommentResponse> getAllComments(@PathVariable Long eventId) {
        return commentService.getCommentsForEvent(eventId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long id,
                              @AuthenticationPrincipal UserSecurity userSecurity) {
        commentService.deleteComment(id, userSecurity.getId());
    }
}
