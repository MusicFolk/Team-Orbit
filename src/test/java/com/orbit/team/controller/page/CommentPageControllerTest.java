package com.orbit.team.controller.page;

import com.orbit.team.dto.request.CommentRequest;
import com.orbit.team.entity.Role;
import com.orbit.team.entity.User;
import com.orbit.team.exception.ResourceNotFoundException;
import com.orbit.team.exception.UnauthorizedActionException;
import com.orbit.team.security.UserSecurity;
import com.orbit.team.service.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CommentPageController.class)
class CommentPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    private UserSecurity currentUser(long id, Role role) {
        User user = User.builder()
                .id(id)
                .username("user")
                .email("user@example.com")
                .password("password")
                .fullName("Full Name")
                .role(role)
                .active(true)
                .build();
        return new UserSecurity(user);
    }

    @Test
    void addComment_callsServiceAndRedirectsToEvent() throws Exception {
        UserSecurity current = currentUser(2L, Role.USER);

        mockMvc.perform(post("/events/{id}/comments", 4L)
                        .param("content", "Great event!")
                        .with(user(current))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/4"));

        verify(commentService).addComment(eq(4L), eq(2L), any(CommentRequest.class));
    }

    @Test
    void addComment_doesNotCallServiceButStillRedirects() throws Exception {
        UserSecurity current = currentUser(2L, Role.USER);

        mockMvc.perform(post("/events/{id}/comments", 4L)
                        .param("content", "   ")
                        .with(user(current))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/4"));

        verifyNoInteractions(commentService);
    }

    @Test
    void addComment_returnsBadRequest() throws Exception {
        UserSecurity current = currentUser(2L, Role.USER);

        mockMvc.perform(post("/events/{id}/comments", 4L)
                        .with(user(current))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(commentService);
    }

    @Test
    void deleteComment_callsServiceAndRedirectsToEvent() throws Exception {
        UserSecurity current = currentUser(2L, Role.USER);

        mockMvc.perform(post("/events/{eventId}/comments/{commentId}/delete", 4L, 6L)
                        .with(user(current))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/4"));

        verify(commentService).deleteComment(6L, 2L);
    }

    @Test
    void deleteComment_notOwnerAndNotAdminReturnsForbidden() throws Exception {
        UserSecurity current = currentUser(2L, Role.USER);

        doThrow(new UnauthorizedActionException("You are not allowed to delete this comment"))
                .when(commentService).deleteComment(6L, 2L);

        mockMvc.perform(post("/events/{eventId}/comments/{commentId}/delete", 4L, 6L)
                        .with(user(current))
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You are not allowed to delete this comment"));
    }

    @Test
    void deleteComment_commentDoesNotExistReturnsNotFound() throws Exception {
        UserSecurity current = currentUser(2L, Role.USER);

        doThrow(new ResourceNotFoundException("Comment not found"))
                .when(commentService).deleteComment(6L, 2L);

        mockMvc.perform(post("/events/{eventId}/comments/{commentId}/delete", 4L, 6L)
                        .with(user(current))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Comment not found"));
    }
}