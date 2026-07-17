package com.orbit.team.controller;

import com.orbit.team.dto.request.CommentRequest;
import com.orbit.team.security.UserSecurity;
import com.orbit.team.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class CommentPageController {

    private final CommentService commentService;

    @PostMapping("/events/{id}/comments")
    public String addComment(@PathVariable Long id, @RequestParam String content,
                             @AuthenticationPrincipal UserSecurity currentUser) {

        if (content != null && !content.isBlank()) {
            CommentRequest request = new CommentRequest();
            request.setContent(content);
            commentService.addComment(id, currentUser.getId(), request);
        }
        return "redirect:/events/" + id;
    }
}
