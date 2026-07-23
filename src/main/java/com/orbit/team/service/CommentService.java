package com.orbit.team.service;

import com.orbit.team.dto.request.CommentRequest;
import com.orbit.team.dto.response.CommentResponse;
import com.orbit.team.entity.Comment;
import com.orbit.team.entity.Event;
import com.orbit.team.entity.Role;
import com.orbit.team.entity.User;
import com.orbit.team.exception.ResourceNotFoundException;
import com.orbit.team.exception.UnauthorizedActionException;
import com.orbit.team.repository.CommentRepository;
import com.orbit.team.repository.EventRepository;
import com.orbit.team.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public CommentResponse addComment(Long eventId, Long userId, CommentRequest request) {
        log.info("User {} adding comment to event {}", userId, eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Cannot add comment. Event {} not found", eventId);
                    return new ResourceNotFoundException("Event not found");
                });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Cannot add comment. User {} not found", userId);
                    return new ResourceNotFoundException("User not found");
                });

        Comment comment = Comment.builder()
                .content(request.getContent())
                .event(event)
                .user(user)
                .build();

        Comment savedComment = commentRepository.save(comment);
        log.info("Comment {} added successfully by user {}", savedComment.getId(), userId);

        return toResponse(savedComment);
    }

    public List<CommentResponse> getCommentsForEvent(Long eventId) {
        log.info("Fetching comments for event {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Cannot fetch comments. Event {} not found", eventId);
                    return new ResourceNotFoundException("Event not found");
                });

        return commentRepository.findByEventOrderByCreatedAtAsc(event)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void deleteComment(Long commentId, Long userId) {
        log.info("User {} attempting to delete comment {}", userId, commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("Cannot delete comment. Comment {} not found", commentId);
                    return new ResourceNotFoundException("Comment not found");
                });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Cannot delete comment. User {} not found", userId);
                    return new ResourceNotFoundException("User not found");
                });

        if (!comment.getUser().getId().equals(userId) && user.getRole() != Role.ADMIN) {
            log.warn("User {} attempted to delete comment {} without permission", userId, commentId);
            throw new UnauthorizedActionException("You are not allowed to delete this comment");
        }

        commentRepository.delete(comment);
        log.info("Comment {} successfully deleted by user {}", commentId, userId);
    }

    private CommentResponse toResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setCommentId(comment.getId());
        response.setContent(comment.getContent());
        response.setUserId(comment.getUser().getId());
        response.setUsername(comment.getUser().getUsername());
        response.setCreatedAt(comment.getCreatedAt());
        return response;
    }
}