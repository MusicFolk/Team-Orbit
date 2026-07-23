package com.orbit.team.controller.page;

import com.orbit.team.dto.response.AttendeeResponse;
import com.orbit.team.entity.Role;
import com.orbit.team.entity.RsvpStatus;
import com.orbit.team.entity.User;
import com.orbit.team.exception.EventFullException;
import com.orbit.team.exception.ResourceNotFoundException;
import com.orbit.team.exception.RsvpConflictException;
import com.orbit.team.security.UserSecurity;
import com.orbit.team.service.RsvpService;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RsvpPageController.class)
class RsvpPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RsvpService rsvpService;

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
    void submitRsvp_newRsvpCreatesAndRedirectsToEvent() throws Exception {
        UserSecurity current = currentUser(2L, Role.USER);
        when(rsvpService.createRsvp(2L, 4L, RsvpStatus.ATTENDING))
                .thenReturn(new AttendeeResponse());

        mockMvc.perform(post("/events/{id}/rsvp", 4L)
                        .param("status", "ATTENDING")
                        .with(user(current))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/4"));

        verify(rsvpService).createRsvp(2L, 4L, RsvpStatus.ATTENDING);
        verify(rsvpService, never()).updateRsvp(anyLong(), anyLong(), any());
    }

    @Test
    void submitRsvp_alreadyExistsFallsBackToUpdateAndRedirects() throws Exception {
        UserSecurity current = currentUser(2L, Role.USER);
        when(rsvpService.createRsvp(2L, 4L, RsvpStatus.MAYBE))
                .thenThrow(new RsvpConflictException("RSVP already exists for this event"));
        when(rsvpService.updateRsvp(2L, 4L, RsvpStatus.MAYBE))
                .thenReturn(new AttendeeResponse());

        mockMvc.perform(post("/events/{id}/rsvp", 4L)
                        .param("status", "MAYBE")
                        .with(user(current))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/4"));

        InOrder inOrder = inOrder(rsvpService);
        inOrder.verify(rsvpService).createRsvp(2L, 4L, RsvpStatus.MAYBE);
        inOrder.verify(rsvpService).updateRsvp(2L, 4L, RsvpStatus.MAYBE);
    }

    @Test
    void submitRsvp_existingRsvpButEventFullOnUpdate_redirectsWithRsvpErrorFull() throws Exception {
        UserSecurity current = currentUser(2L, Role.USER);
        when(rsvpService.createRsvp(2L, 4L, RsvpStatus.ATTENDING))
                .thenThrow(new RsvpConflictException("RSVP already exists for this event"));
        when(rsvpService.updateRsvp(2L, 4L, RsvpStatus.ATTENDING))
                .thenThrow(new EventFullException("Event has reached its attendee capacity"));

        mockMvc.perform(post("/events/{id}/rsvp", 4L)
                        .param("status", "ATTENDING")
                        .with(user(current))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/4?rsvpError=full"));
    }

    @Test
    void submitRsvp_eventFullOnCreateRedirectsWithRsvpErrorFull() throws Exception {
        UserSecurity current = currentUser(2L, Role.USER);
        when(rsvpService.createRsvp(2L, 4L, RsvpStatus.ATTENDING))
                .thenThrow(new EventFullException("Event has reached its attendee capacity"));

        mockMvc.perform(post("/events/{id}/rsvp", 4L)
                        .param("status", "ATTENDING")
                        .with(user(current))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/4?rsvpError=full"));

        verify(rsvpService, never()).updateRsvp(anyLong(), anyLong(), any());
    }

    @Test
    void submitRsvp_eventNotFoundReturnsNotFound() throws Exception {
        UserSecurity current = currentUser(2L, Role.USER);
        when(rsvpService.createRsvp(2L, 4L, RsvpStatus.ATTENDING))
                .thenThrow(new ResourceNotFoundException("Event not found: 4"));

        mockMvc.perform(post("/events/{id}/rsvp", 4L)
                        .param("status", "ATTENDING")
                        .with(user(current))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Event not found: 4"));
    }

    @Test
    void submitRsvp_missingStatusParamReturnsBadRequest() throws Exception {
        UserSecurity current = currentUser(2L, Role.USER);

        mockMvc.perform(post("/events/{id}/rsvp", 4L)
                        .with(user(current))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(rsvpService);
    }

    @Test
    void submitRsvp_invalidStatusValueReturnsBadRequest() throws Exception {
        UserSecurity current = currentUser(2L, Role.USER);

        mockMvc.perform(post("/events/{id}/rsvp", 4L)
                        .param("status", "ANY")
                        .with(user(current))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(rsvpService);
    }
}