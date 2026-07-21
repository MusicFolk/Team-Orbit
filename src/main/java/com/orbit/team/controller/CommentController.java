package com.orbit.team.controller;

import com.orbit.team.dto.request.CommentRequest;
import com.orbit.team.dto.response.CommentResponse;
import com.orbit.team.security.UserSecurity;
import com.orbit.team.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events/{eventId}/comments")
@RequiredArgsConstructor
@Tag(name = "Comments")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "Add comment to an event")
    @ApiResponse(responseCode = "201", description = "Comment created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Event not found")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse addComment(
            @PathVariable Long eventId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserSecurity userSecurity) {

        return commentService.addComment(eventId, userSecurity.getId(), request);
    }

    @Operation(summary = "Get all comments for an event")
    @ApiResponse(responseCode = "200", description = "Comments returned successfully")
    @ApiResponse(responseCode = "404", description = "Event not found")
    @GetMapping
    public List<CommentResponse> getComments(@PathVariable Long eventId) {
        return commentService.getCommentsForEvent(eventId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable Long eventId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserSecurity userSecurity) {

        commentService.deleteComment(commentId, userSecurity.getId());
    }
}