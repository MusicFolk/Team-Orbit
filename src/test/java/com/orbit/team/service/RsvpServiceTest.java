package com.orbit.team.service;

import com.orbit.team.dto.response.AttendeeResponse;
import com.orbit.team.entity.Event;
import com.orbit.team.entity.RSVP;
import com.orbit.team.entity.RsvpStatus;
import com.orbit.team.entity.User;
import com.orbit.team.exception.EventFullException;
import com.orbit.team.exception.ResourceNotFoundException;
import com.orbit.team.exception.RsvpConflictException;
import com.orbit.team.exception.UnauthorizedActionException;
import com.orbit.team.repository.EventRepository;
import com.orbit.team.repository.RsvpRepository;
import com.orbit.team.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RsvpServiceTest {

    @Mock
    private RsvpRepository rsvpRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RsvpService rsvpService;

    private User user;
    private Event event;
    private RSVP rsvp;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("ehaaland00").fullName("Erling Haaland").build();
        event = Event.builder().id(1L).title("Test Event").organizer(user).capacity(10).build();
        rsvp = RSVP.builder().user(user).event(event).status(RsvpStatus.ATTENDING).build();
    }

    // createRsvp
    @Test
    void createRsvp_shouldCreateWhenNoneExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(rsvpRepository.findByUserAndEvent(user, event)).thenReturn(Optional.empty());
        when(rsvpRepository.save(any(RSVP.class))).thenReturn(rsvp);

        AttendeeResponse result = rsvpService.createRsvp(1L, 1L, RsvpStatus.ATTENDING);

        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(RsvpStatus.ATTENDING);
        verify(rsvpRepository).save(any(RSVP.class));
    }

    @Test
    void createRsvp_shouldThrowWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rsvpService.createRsvp(99L, 1L, RsvpStatus.ATTENDING))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(rsvpRepository, never()).save(any());
    }

    @Test
    void createRsvp_shouldThrowWhenEventNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rsvpService.createRsvp(1L, 99L, RsvpStatus.ATTENDING))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Event not found");

        verify(rsvpRepository, never()).save(any());
    }

    @Test
    void createRsvp_shouldThrowWhenAlreadyExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(rsvpRepository.findByUserAndEvent(user, event)).thenReturn(Optional.of(rsvp));

        assertThatThrownBy(() -> rsvpService.createRsvp(1L, 1L, RsvpStatus.ATTENDING))
                .isInstanceOf(RsvpConflictException.class)
                .hasMessageContaining("RSVP already exists");

        verify(rsvpRepository, never()).save(any());
    }

    @Test
    void createRsvp_shouldThrowConflictOnDataIntegrityViolation() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(rsvpRepository.findByUserAndEvent(user, event)).thenReturn(Optional.empty());
        when(rsvpRepository.save(any(RSVP.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> rsvpService.createRsvp(1L, 1L, RsvpStatus.ATTENDING))
                .isInstanceOf(RsvpConflictException.class)
                .hasMessageContaining("data conflict");
    }

    @Test
    void createRsvp_shouldThrowEventFullWhenCapacityReached() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(rsvpRepository.findByUserAndEvent(user, event)).thenReturn(Optional.empty());
        when(rsvpRepository.countByEvent_IdAndStatus(1L, RsvpStatus.ATTENDING)).thenReturn(10L);

        assertThatThrownBy(() -> rsvpService.createRsvp(1L, 1L, RsvpStatus.ATTENDING))
                .isInstanceOf(EventFullException.class)
                .hasMessageContaining("capacity");

        verify(rsvpRepository, never()).save(any());
    }

    @Test
    void createRsvp_shouldAllowNonAttendingStatusWhenCapacityReached() {
        RSVP maybeRsvp = RSVP.builder().user(user).event(event).status(RsvpStatus.MAYBE).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(rsvpRepository.findByUserAndEvent(user, event)).thenReturn(Optional.empty());
        when(rsvpRepository.save(any(RSVP.class))).thenReturn(maybeRsvp);

        AttendeeResponse result = rsvpService.createRsvp(1L, 1L, RsvpStatus.MAYBE);

        assertThat(result.getStatus()).isEqualTo(RsvpStatus.MAYBE);
        verify(rsvpRepository, never()).countByEvent_IdAndStatus(any(), any());
        verify(rsvpRepository).save(any(RSVP.class));
    }

    // updateRsvp
    @Test
    void updateRsvp_shouldUpdateStatusWhenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(rsvpRepository.findByUserAndEvent(user, event)).thenReturn(Optional.of(rsvp));
        when(rsvpRepository.save(any(RSVP.class))).thenReturn(rsvp);

        AttendeeResponse result = rsvpService.updateRsvp(1L, 1L, RsvpStatus.MAYBE);

        assertThat(result).isNotNull();
        verify(rsvpRepository).save(rsvp);
    }

    @Test
    void updateRsvp_shouldThrowWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rsvpService.updateRsvp(99L, 1L, RsvpStatus.MAYBE))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateRsvp_shouldThrowWhenEventNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rsvpService.updateRsvp(1L, 99L, RsvpStatus.MAYBE))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Event not found");
    }

    @Test
    void updateRsvp_shouldThrowWhenNoExistingRsvp() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(rsvpRepository.findByUserAndEvent(user, event)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rsvpService.updateRsvp(1L, 1L, RsvpStatus.MAYBE))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No existing RSVP");

        verify(rsvpRepository, never()).save(any());
    }

    @Test
    void updateRsvp_shouldThrowConflictOnDataIntegrityViolation() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(rsvpRepository.findByUserAndEvent(user, event)).thenReturn(Optional.of(rsvp));
        when(rsvpRepository.save(any(RSVP.class))).thenThrow(new DataIntegrityViolationException("conflict"));

        assertThatThrownBy(() -> rsvpService.updateRsvp(1L, 1L, RsvpStatus.MAYBE))
                .isInstanceOf(RsvpConflictException.class)
                .hasMessageContaining("data conflict");
    }

    @Test
    void updateRsvp_shouldThrowEventFullWhenTransitioningToAttendingAndCapacityReached() {
        RSVP maybeRsvp = RSVP.builder().user(user).event(event).status(RsvpStatus.MAYBE).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(rsvpRepository.findByUserAndEvent(user, event)).thenReturn(Optional.of(maybeRsvp));
        when(rsvpRepository.countByEvent_IdAndStatus(1L, RsvpStatus.ATTENDING)).thenReturn(10L);

        assertThatThrownBy(() -> rsvpService.updateRsvp(1L, 1L, RsvpStatus.ATTENDING))
                .isInstanceOf(EventFullException.class)
                .hasMessageContaining("capacity");

        verify(rsvpRepository, never()).save(any());
    }

    @Test
    void updateRsvp_shouldAllowWhenAlreadyAttendingEvenIfCapacityReached() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(rsvpRepository.findByUserAndEvent(user, event)).thenReturn(Optional.of(rsvp));
        when(rsvpRepository.save(any(RSVP.class))).thenReturn(rsvp);

        AttendeeResponse result = rsvpService.updateRsvp(1L, 1L, RsvpStatus.ATTENDING);

        assertThat(result).isNotNull();
        verify(rsvpRepository, never()).countByEvent_IdAndStatus(any(), any());
        verify(rsvpRepository).save(rsvp);
    }

    // cancelRsvp
    @Test
    void cancelRsvp_shouldDeleteWhenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(rsvpRepository.findByUserAndEvent(user, event)).thenReturn(Optional.of(rsvp));

        rsvpService.cancelRsvp(1L, 1L);

        verify(rsvpRepository).delete(rsvp);
    }

    @Test
    void cancelRsvp_shouldDoNothingWhenNoneExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(rsvpRepository.findByUserAndEvent(user, event)).thenReturn(Optional.empty());

        rsvpService.cancelRsvp(1L, 1L);

        verify(rsvpRepository, never()).delete(any());
    }

    @Test
    void cancelRsvp_shouldThrowWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rsvpService.cancelRsvp(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    // getRsvpsForEvent
    @Test
    void getRsvpsForEvent_shouldReturnListWhenOrganizer() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(rsvpRepository.findByEvent_Id(1L)).thenReturn(List.of(rsvp));

        List<AttendeeResponse> result = rsvpService.getRsvpsForEvent(1L, event.getOrganizer().getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    void getRsvpsForEvent_shouldThrowWhenEventNotFound() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rsvpService.getRsvpsForEvent(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Event not found");
    }

    @Test
    void getRsvpsForEvent_shouldThrowWhenCallerNotOrganizer() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> rsvpService.getRsvpsForEvent(1L, 999L))
                .isInstanceOf(UnauthorizedActionException.class)
                .hasMessageContaining("Only the organizer");

        verify(rsvpRepository, never()).findByEvent_Id(any());
    }

}