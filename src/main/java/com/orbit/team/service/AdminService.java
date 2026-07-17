package com.orbit.team.service;

import com.orbit.team.entity.Comment;
import com.orbit.team.entity.Event;
import com.orbit.team.entity.User;
import com.orbit.team.repository.CommentRepository;
import com.orbit.team.repository.EventRepository;
import com.orbit.team.repository.UserRepository;
import com.orbit.team.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentRepository commentRepository;

    public List<User> getAllUsers() {
        log.info("Admin fetching all users");
        return userRepository.findAll();
    }

    public User deactivateUser(Long userId) {
        log.info("Admin deactivating user {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        user.setActive(false);

        log.info("User {} successfully deactivated", userId);
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        log.info("Admin deleting user {}", userId);

        if (!userRepository.existsById(userId)) {
            log.warn("Cannot delete user {}. User not found", userId);
            throw new ResourceNotFoundException("User not found: " + userId);
        }
        userRepository.deleteById(userId);
        log.info("User {} successfully deleted", userId);
    }

    public List<Event> getAllEvents() {
        log.info("Admin fetching all events");
        return eventRepository.findAll();
    }

    public void deleteEvent(Long eventId) {
        log.info("Admin deleting event {}", eventId);

        if (!eventRepository.existsById(eventId)) {
            log.warn("Cannot delete event {}. Event not found", eventId);
            throw new ResourceNotFoundException("Event not found: " + eventId);
        }
        eventRepository.deleteById(eventId);
        log.info("Event {} successfully deleted", eventId);
    }

    public List<Comment> getAllComments() {
        log.info("Admin fetching all comments");
        return commentRepository.findAll();
    }

    public void deleteComment(Long commentId) {
        log.info("Admin deleting comment {}", commentId);

        if (!commentRepository.existsById(commentId)) {
            log.warn("Cannot delete comment {}. Comment not found", commentId);
            throw new ResourceNotFoundException("Comment not found: " + commentId);
        }
        commentRepository.deleteById(commentId);
        log.info("Comment {} successfully deleted", commentId);
    }
}