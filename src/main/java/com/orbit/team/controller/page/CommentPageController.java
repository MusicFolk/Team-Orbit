package com.orbit.team.controller.page;

import com.orbit.team.dto.request.CommentRequest;
import com.orbit.team.security.UserSecurity;
import com.orbit.team.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class CommentPageController {

    private final CommentService commentService;

    @PostMapping("/events/{id}/comments")
    public String addComment(@PathVariable Long id,
                             @RequestParam String content,
                             @AuthenticationPrincipal UserSecurity currentUser) {

        if (content != null && !content.isBlank()) {
            CommentRequest request = new CommentRequest();
            request.setContent(content);
            commentService.addComment(id, currentUser.getId(), request);
        }

        return "redirect:/events/" + id;
    }

    @PostMapping("/events/{eventId}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Long eventId,
                                @PathVariable Long commentId,
                                @AuthenticationPrincipal UserSecurity currentUser) {

        commentService.deleteComment(commentId, currentUser.getId());

        return "redirect:/events/" + eventId;
    }
}