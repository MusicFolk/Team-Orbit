package com.orbit.team.service;

import com.orbit.team.entity.Comment;
import com.orbit.team.entity.Event;
import com.orbit.team.entity.User;
import com.orbit.team.exception.ResourceNotFoundException;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    // deactivate a user
    @Test
    void deactivateUser_shouldSetActiveFalseAndSave() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = adminService.deactivateUser(1L);

        assertThat(result.isActive()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void deactivateUser_shouldThrowWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.deactivateUser(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository, never()).save(any());
    }

    // delete a user
    @Test
    void deleteUser_shouldDeleteWhenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        adminService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_shouldThrowWhenUserNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> adminService.deleteUser(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository, never()).deleteById(any());
    }

    // get all events
    @Test
    void getAllEvents_shouldReturnList() {
        when(eventRepository.findAll()).thenReturn(List.of(event));

        List<Event> result = adminService.getAllEvents();

        assertThat(result).hasSize(1).containsExactly(event);
    }

    // delete an event
    @Test
    void deleteEvent_shouldDeleteWhenExists() {
        when(eventRepository.existsById(1L)).thenReturn(true);

        adminService.deleteEvent(1L);

        verify(eventRepository).deleteById(1L);
    }

    @Test
    void deleteEvent_shouldThrowWhenEventNotFound() {
        when(eventRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> adminService.deleteEvent(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Event not found");

        verify(eventRepository, never()).deleteById(any());
    }


    // fetch all comments
    @Test
    void getAllComments_shouldReturnList() {
        when(commentRepository.findAll()).thenReturn(List.of(comment));

        List<Comment> result = adminService.getAllComments();

        assertThat(result).hasSize(1).containsExactly(comment);
    }


    // delete a comment
    @Test
    void deleteComment_shouldDeleteWhenExists() {
        when(commentRepository.existsById(1L)).thenReturn(true);

        adminService.deleteComment(1L);

        verify(commentRepository).deleteById(1L);
    }

    @Test
    void deleteComment_shouldThrowWhenCommentNotFound() {
        when(commentRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> adminService.deleteComment(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Comment not found");

        verify(commentRepository, never()).deleteById(any());
    }
}f
