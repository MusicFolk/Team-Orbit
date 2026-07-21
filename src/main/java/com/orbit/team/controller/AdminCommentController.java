package com.orbit.team.controller;

import com.orbit.team.dto.response.CommentResponse;
import com.orbit.team.security.UserSecurity;
import com.orbit.team.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin - Comments")
public class AdminCommentController {

    private final CommentService commentService;

    @Operation(summary = "Get all comments for an event as admin")
    @ApiResponse(responseCode = "200", description = "Comments returned successfully")
    @ApiResponse(responseCode = "403", description = "Requires ADMIN role")
    @ApiResponse(responseCode = "404", description = "Event not found")
    @GetMapping("/{eventId}")
    public List<CommentResponse> getAllComments(@PathVariable Long eventId) {
        return commentService.getCommentsForEvent(eventId);
    }

    @Operation(summary = "Delete a comment as admin")
    @ApiResponse(responseCode = "204", description = "Comment deleted successfully")
    @ApiResponse(responseCode = "403", description = "Requires ADMIN role")
    @ApiResponse(responseCode = "404", description = "Comment or user not found")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long id,
                              @AuthenticationPrincipal UserSecurity userSecurity) {
        commentService.deleteComment(id, userSecurity.getId());
    }
}
