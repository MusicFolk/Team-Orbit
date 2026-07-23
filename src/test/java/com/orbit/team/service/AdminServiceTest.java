package com.orbit.team.service;

import com.orbit.team.entity.Comment;
import com.orbit.team.entity.Event;
import com.orbit.team.entity.User;
import com.orbit.team.repository.CommentRepository;
import com.orbit.team.repository.EventRepository;
import com.orbit.team.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private AdminService adminService;

    private User user;
    private Event event;
    private Comment comment;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("someUser").active(true).build();
        event = Event.builder().id(1L).title("Test Event").build();
        comment = Comment.builder().id(1L).content("Nice event").build();
    }

    // get all users
    @Test
    void getAllUsers_shouldReturnList() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> result = adminService.getAllUsers();

        assertThat(result).hasSize(1).containsExactly(user);
    }

    // get all events
    @Test
    void getAllEvents_shouldReturnList() {
        when(eventRepository.findAll()).thenReturn(List.of(event));

        List<Event> result = adminService.getAllEvents();

        assertThat(result).hasSize(1).containsExactly(event);
    }

    // fetch all comments
    @Test
    void getAllComments_shouldReturnList() {
        when(commentRepository.findAll()).thenReturn(List.of(comment));

        List<Comment> result = adminService.getAllComments();

        assertThat(result).hasSize(1).containsExactly(comment);
    }
}
