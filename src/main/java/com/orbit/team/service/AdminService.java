package com.orbit.team.service;

import com.orbit.team.entity.Comment;
import com.orbit.team.entity.Event;
import com.orbit.team.entity.User;
import com.orbit.team.repository.CommentRepository;
import com.orbit.team.repository.EventRepository;
import com.orbit.team.repository.UserRepository;
import com.orbit.team.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentRepository commentRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment not found: " + commentId);
        }
        commentRepository.deleteById(commentId);
    }
}