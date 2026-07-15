package com.orbit.team.service;

import com.orbit.team.dto.request.CommentRequest;
import com.orbit.team.dto.response.CommentResponse;
import com.orbit.team.entity.Comment;
import com.orbit.team.entity.Event;
import com.orbit.team.entity.Role;
import com.orbit.team.entity.User;
import com.orbit.team.repository.CommentRepository;
import com.orbit.team.repository.EventRepository;
import com.orbit.team.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public CommentResponse addComment(Long eventId, Long userId, CommentRequest request) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .event(event)
                .user(user)
                .build();

        Comment savedComment = commentRepository.save(comment);

        return toResponse(savedComment);
    }

    @Override
    public List<CommentResponse> getCommentsForEvent(Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        return commentRepository.findByEventOrderByCreatedAtAsc(event)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Only admins can delete comments");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        commentRepository.delete(comment);
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