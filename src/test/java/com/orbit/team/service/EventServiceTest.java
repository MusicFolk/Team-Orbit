package com.orbit.team.service;

import com.orbit.team.dto.request.EventRequest;
import com.orbit.team.entity.Event;
import com.orbit.team.entity.EventCategory;
import com.orbit.team.entity.User;
import com.orbit.team.exception.ResourceNotFoundException;
import com.orbit.team.exception.UnauthorizedActionException;
import com.orbit.team.repository.CommentRepository;
import com.orbit.team.repository.EventRepository;
import com.orbit.team.repository.RsvpRepository;
import com.orbit.team.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RsvpRepository rsvpRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private EventService eventService;

    private User organizer;
    private Event event;
    private EventRequest request;

    @BeforeEach
    void setUp() {
        organizer = User.builder().id(1L).username("organizer").build();

        event = Event.builder()
                .id(1L)
                .title("Test Event")
                .organizer(organizer)
                .category(EventCategory.MUSIC)
                .build();

        request = new EventRequest();
        request.setTitle("New Event");
        request.setDescription("Description");
        request.setLocation("Riga");
        request.setEventDateTime(LocalDateTime.now().plusDays(1));
        request.setCapacity(50);
        request.setCategory(EventCategory.MUSIC);
    }

    @Test
    void createEvent_shouldSaveAndReturnEvent() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(organizer));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        Event result = eventService.createEvent(request, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getCategory()).isEqualTo(EventCategory.MUSIC);

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void createEvent_shouldThrowWhenUserNotFound() {

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.createEvent(request, 99L)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getEventById_shouldReturnEvent() {

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        Event result = eventService.getEventById(1L);
        assertThat(result).isEqualTo(event);
    }

    @Test
    void getEventById_shouldThrowWhenNotFound() {

        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventById(999L)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Event not found");
    }

    @Test
    void updateEvent_shouldSucceedForOrganizer() {

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        Event result = eventService.updateEvent(1L, request, 1L);
        assertThat(result).isNotNull();

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void updateEvent_shouldThrowWhenNoOrganizer() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.updateEvent(1L, request, 2L))
                .isInstanceOf(UnauthorizedActionException.class)
                .hasMessageContaining("Only the organizer");

        verify(eventRepository, never()).save(any());
    }

    @Test
    void deleteEvent_shouldSucceedForOrganizer() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(rsvpRepository.findByEvent(event)).thenReturn(List.of());
        when(commentRepository.findByEventOrderByCreatedAtAsc(event)).thenReturn(List.of());

        eventService.deleteEvent(1L, 1L);

        verify(eventRepository).delete(event);
        verify(rsvpRepository).deleteAll(List.of());
        verify(commentRepository).deleteAll(List.of());
    }

    @Test
    void deleteEvent_shouldThrowWhenNoOrganizer() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.deleteEvent(1L, 2L)).isInstanceOf(UnauthorizedActionException.class)
                .hasMessageContaining("Only the organizer");

        verify(eventRepository, never()).delete(any());
    }

    @Test
    void getAllEvents_shouldReturnList() {

        when(eventRepository.findAll()).thenReturn(List.of(event));

        List<Event> result = eventService.getAllEvents();
        assertThat(result).hasSize(1).containsExactly(event);
    }
}
