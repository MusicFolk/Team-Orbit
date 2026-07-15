package com.orbit.team.controller;

import com.orbit.team.entity.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
@EnableMethodSecurity
@PreAuthorize("hasRole('ADMIN')")
public class AdminCommentController {

    //private CommentService commentService;

    @GetMapping("/{eventId}")
    public List<Comment> getAllComments(@PathVariable Long eventId) {
        return null;
    }

    @DeleteMapping("/{id}")
    public void deleteComment(@PathVariable Long id) {}

}
