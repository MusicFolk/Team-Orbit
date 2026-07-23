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

    public List<Event> getAllEvents() {
        log.info("Admin fetching all events");
        return eventRepository.findAll();
    }

    public List<Comment> getAllComments() {
        log.info("Admin fetching all comments");
        return commentRepository.findAll();
    }
}