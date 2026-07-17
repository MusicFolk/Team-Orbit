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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private User user;
    private User admin;
    private Event event;
    private Comment comment;
    private CommentRequest commentRequest;

    @BeforeEach
    void setUp() {
        user = User.builder().id(2L).username("orbits").role(Role.USER).build();
        admin = User.builder().id(8L).username("admin").role(Role.ADMIN).build();
        event = Event.builder().id(4L).title("Orbit event").build();
        comment = Comment.builder().id(6L).content("Random comment").event(event).user(user).build();
        commentRequest = new CommentRequest();
        commentRequest.setContent("Random comment");
    }

    @Test
    void addComment_shouldSaveAndReturnComment() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(4L)).thenReturn(Optional.of(event));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentResponse commentResponse = commentService.addComment(4L,2L,commentRequest);

        assertThat(commentResponse.getCommentId()).isEqualTo(6L);
        assertThat(commentResponse.getContent()).isEqualTo("Random comment");
        assertThat(commentResponse.getUserId()).isEqualTo(2L);
        assertThat(commentResponse.getUsername()).isEqualTo("orbits");

        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addComment_shouldThrowExceptionWhenUserIsNotFound() {
        when(eventRepository.findById(4L)).thenReturn(Optional.of(event));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.addComment(4L, 2L, commentRequest));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void addComment_shouldThrowExceptionWhenEventIsNotFound() {
        when(eventRepository.findById(4L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.addComment(4L, 2L, commentRequest));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void getCommentsForEvent_shouldReturnCommentsWhenEventExists() {
        when(eventRepository.findById(4L)).thenReturn(Optional.of(event));
        when(commentRepository.findByEventOrderByCreatedAtAsc(event)).thenReturn(List.of(comment));

        List<CommentResponse> result = commentService.getCommentsForEvent(4L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCommentId()).isEqualTo(6L);
        assertThat(result.get(0).getContent()).isEqualTo("Random comment");
    }

    @Test
    void getCommentsForEvent_shouldReturnEmptyListWhenThereAreNoComments() {
        when(eventRepository.findById(4L)).thenReturn(Optional.of(event));
        when(commentRepository.findByEventOrderByCreatedAtAsc(event)).thenReturn(List.of());

        List<CommentResponse> result = commentService.getCommentsForEvent(4L);
        assertThat(result).isEmpty();
    }

    @Test
    void getCommentsForEvent_shouldThrowExceptionWhenThereIsNoEvent() {
        when(eventRepository.findById(4L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.getCommentsForEvent(4L));
        verify(commentRepository, never()).findByEventOrderByCreatedAtAsc(any());
    }

    @Test
    void deleteComment_shouldDeleteCommentIfUserIsAdmin() {
        when(userRepository.findById(8L)).thenReturn(Optional.of(admin));
        when(commentRepository.findById(6L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(6L, 8L);
        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_shouldThrowExceptionWhenUserIsNotAdmin() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        assertThrows(UnauthorizedActionException.class, () -> commentService.deleteComment(6L, 2L));
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void deleteComment_shouldThrowExceptionWhenThereIsNoUser() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.deleteComment(6L, 2L));
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void deleteComment_shouldThrowExceptionWhenThereIsNoComment() {
        when(userRepository.findById(8L)).thenReturn(Optional.of(admin));
        when(commentRepository.findById(6L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.deleteComment(6L, 8L));
        verify(commentRepository, never()).delete(any());
    }
}
