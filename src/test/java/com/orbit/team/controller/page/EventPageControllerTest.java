package com.orbit.team.controller.page;

import com.orbit.team.dto.request.EventRequest;
import com.orbit.team.dto.response.AttendeeResponse;
import com.orbit.team.entity.*;
import com.orbit.team.security.UserSecurity;
import com.orbit.team.service.CommentService;
import com.orbit.team.service.EventService;
import com.orbit.team.service.RsvpService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventPageController.class)
public class EventPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private RsvpService rsvpService;

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

    private Event event(long id, long organizerId, LocalDateTime dateTime) {
        User organizer = User.builder()
                .id(organizerId)
                .username("organizer")
                .fullName("Organizer Name")
                .role(Role.USER)
                .active(true)
                .build();

        return Event.builder()
                .id(id)
                .title("Event " + id)
                .description("Description")
                .location("Riga")
                .category(EventCategory.MUSIC)
                .eventDateTime(dateTime)
                .capacity(50)
                .organizer(organizer)
                .build();
    }

    private String futureDateTimeParam() {
        return LocalDateTime.now().plusMonths(6).withHour(18).withMinute(0)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }

    @Test
    void dashboard_noDates_returnsAllEvents() throws Exception {
        Event early = event(1L, 1L, LocalDateTime.of(2026, 1, 5, 18, 0));
        Event mid = event(2L, 1L, LocalDateTime.of(2026, 2, 15, 18, 0));

        when(eventService.getAllEvents()).thenReturn(List.of(early, mid));

        mockMvc.perform(get("/events").with(user(currentUser(1L, Role.USER))))
                .andExpect(status().isOk())
                .andExpect(view().name("events"))
                .andExpect(model().attribute("events", hasSize(2)));
    }

    @Test
    void dashboard_onlyFromDate_filtersEventsOnOrAfter() throws Exception {
        Event early = event(1L, 1L, LocalDateTime.of(2026, 1, 5, 18, 0));
        Event late = event(2L, 1L, LocalDateTime.of(2026, 3, 25, 18, 0));

        when(eventService.getAllEvents()).thenReturn(List.of(early, late));

        mockMvc.perform(get("/events")
                        .param("fromDate", "2026-02-01")
                        .with(user(currentUser(1L, Role.USER))))
                .andExpect(status().isOk())
                .andExpect(view().name("events"))
                .andExpect(model().attribute("events", hasSize(1)));
    }

    @Test
    void dashboard_onlyToDate_filtersEventsOnOrBefore() throws Exception {
        Event early = event(1L, 1L, LocalDateTime.of(2026, 1, 5, 18, 0));
        Event late = event(2L, 1L, LocalDateTime.of(2026, 3, 25, 18, 0));

        when(eventService.getAllEvents()).thenReturn(List.of(early, late));

        mockMvc.perform(get("/events")
                        .param("toDate", "2026-02-01")
                        .with(user(currentUser(1L, Role.USER))))
                .andExpect(status().isOk())
                .andExpect(view().name("events"))
                .andExpect(model().attribute("events", hasSize(1)));
    }

    @Test
    void dashboard_bothDates_filtersEventsWithinRange() throws Exception {
        Event early = event(1L, 1L, LocalDateTime.of(2026, 1, 5, 18, 0));
        Event mid = event(2L, 1L, LocalDateTime.of(2026, 2, 15, 18, 0));
        Event late = event(3L, 1L, LocalDateTime.of(2026, 3, 25, 18, 0));

        when(eventService.getAllEvents()).thenReturn(List.of(early, mid, late));

        mockMvc.perform(get("/events")
                        .param("fromDate", "2026-01-10")
                        .param("toDate", "2026-03-01")
                        .with(user(currentUser(1L, Role.USER))))
                .andExpect(status().isOk())
                .andExpect(view().name("events"))
                .andExpect(model().attribute("events", hasSize(1)));
    }

    @Test
    void dashboard_fromDateAfterToDate_returnsErrorAndNoEvents() throws Exception {
        Event mid = event(1L, 1L, LocalDateTime.of(2026, 2, 15, 18, 0));

        when(eventService.getAllEvents()).thenReturn(List.of(mid));

        mockMvc.perform(get("/events")
                        .param("fromDate", "2026-03-01")
                        .param("toDate", "2026-01-01")
                        .with(user(currentUser(1L, Role.USER))))
                .andExpect(status().isOk())
                .andExpect(view().name("events"))
                .andExpect(model().attribute("events", hasSize(0)))
                .andExpect(model().attribute("error", "From date must be before or equal to To date."));
    }

    @Test
    void dashboard_existingFiltersStillWork() throws Exception {
        Event mid = event(1L, 1L, LocalDateTime.of(2026, 2, 15, 18, 0));
        mid.setTitle("Jazz Night");

        when(eventService.getAllEvents()).thenReturn(List.of(mid));

        mockMvc.perform(get("/events")
                        .param("keyword", "Jazz")
                        .param("category", "MUSIC")
                        .with(user(currentUser(1L, Role.USER))))
                .andExpect(status().isOk())
                .andExpect(view().name("events"))
                .andExpect(model().attribute("events", hasSize(1)));
    }

    @Test
    void dashboard_keywordFilter_excludesNonMatchingEvents() throws Exception {
        Event jazz = event(1L, 1L, LocalDateTime.of(2026, 2, 15, 18, 0));
        jazz.setTitle("Jazz Night");
        Event rock = event(2L, 1L, LocalDateTime.of(2026, 2, 16, 18, 0));
        rock.setTitle("Rock Show");

        when(eventService.getAllEvents()).thenReturn(List.of(jazz, rock));

        mockMvc.perform(get("/events")
                        .param("keyword", "Jazz")
                        .with(user(currentUser(1L, Role.USER))))
                .andExpect(status().isOk())
                .andExpect(view().name("events"))
                .andExpect(model().attribute("events", hasSize(1)));
    }

    @Test
    void dashboard_cityFilter_matchesAndExcludes() throws Exception {
        Event riga = event(1L, 1L, LocalDateTime.of(2026, 2, 15, 18, 0));
        riga.setLocation("Riga");
        Event vilnius = event(2L, 1L, LocalDateTime.of(2026, 2, 16, 18, 0));
        vilnius.setLocation("Vilnius");

        when(eventService.getAllEvents()).thenReturn(List.of(riga, vilnius));

        mockMvc.perform(get("/events")
                        .param("city", "Riga")
                        .with(user(currentUser(1L, Role.USER))))
                .andExpect(status().isOk())
                .andExpect(view().name("events"))
                .andExpect(model().attribute("events", hasSize(1)));
    }

    @Test
    void dashboard_categoryFilter_excludesNonMatchingEvents() throws Exception {
        Event music = event(1L, 1L, LocalDateTime.of(2026, 2, 15, 18, 0));
        Event sports = event(2L, 1L, LocalDateTime.of(2026, 2, 16, 18, 0));
        sports.setCategory(EventCategory.SPORTS);

        when(eventService.getAllEvents()).thenReturn(List.of(music, sports));

        mockMvc.perform(get("/events")
                        .param("category", "MUSIC")
                        .with(user(currentUser(1L, Role.USER))))
                .andExpect(status().isOk())
                .andExpect(view().name("events"))
                .andExpect(model().attribute("events", hasSize(1)));
    }

    @Test
    void dashboard_blankKeyword_isTreatedAsNoFilter() throws Exception {
        Event event1 = event(1L, 1L, LocalDateTime.of(2026, 2, 15, 18, 0));
        Event event2 = event(2L, 1L, LocalDateTime.of(2026, 2, 16, 18, 0));

        when(eventService.getAllEvents()).thenReturn(List.of(event1, event2));

        mockMvc.perform(get("/events")
                        .param("keyword", "   ")
                        .with(user(currentUser(1L, Role.USER))))
                .andExpect(status().isOk())
                .andExpect(view().name("events"))
                .andExpect(model().attribute("events", hasSize(2)));
    }

    @Test
    void dashboard_keywordFilter_handlesNullDescription() throws Exception {
        Event event1 = event(1L, 1L, LocalDateTime.of(2026, 2, 15, 18, 0));
        event1.setDescription(null);

        when(eventService.getAllEvents()).thenReturn(List.of(event1));

        mockMvc.perform(get("/events")
                        .param("keyword", "something")
                        .with(user(currentUser(1L, Role.USER))))
                .andExpect(status().isOk())
                .andExpect(view().name("events"))
                .andExpect(model().attribute("events", hasSize(0)));
    }

    @Test
    void eventDetail_organizerSeesAllRsvpStatuses() throws Exception {
        Event event = event(4L, 1L, LocalDateTime.of(2026, 2, 15, 18, 0));

        AttendeeResponse attending = new AttendeeResponse();
        attending.setStatus(RsvpStatus.ATTENDING);
        AttendeeResponse maybe = new AttendeeResponse();
        maybe.setStatus(RsvpStatus.MAYBE);
        AttendeeResponse no = new AttendeeResponse();
        no.setStatus(RsvpStatus.NO);

        when(eventService.getEventById(4L)).thenReturn(event);
        when(rsvpService.getRsvpsForEvent(4L, 1L)).thenReturn(List.of(attending, maybe, no));
        when(commentService.getCommentsForEvent(4L)).thenReturn(List.of());

        mockMvc.perform(get("/events/{id}", 4L).with(user(currentUser(1L, Role.USER))))
                .andExpect(status().isOk())
                .andExpect(view().name("event_detail"))
                .andExpect(model().attribute("isOrganizer", true))
                .andExpect(model().attribute("attendees", hasSize(3)));

        verify(rsvpService).getRsvpsForEvent(4L, 1L);
    }

    @Test
    void eventDetail_nonOrganizerDoesNotSeeAttendees() throws Exception {
        Event event = event(4L, 1L, LocalDateTime.of(2026, 2, 15, 18, 0));

        when(eventService.getEventById(4L)).thenReturn(event);
        when(commentService.getCommentsForEvent(4L)).thenReturn(List.of());

        mockMvc.perform(get("/events/{id}", 4L).with(user(currentUser(2L, Role.USER))))
                .andExpect(status().isOk())
                .andExpect(view().name("event_detail"))
                .andExpect(model().attribute("isOrganizer", false))
                .andExpect(model().attributeDoesNotExist("attendees"));

        verify(rsvpService, org.mockito.Mockito.never()).getRsvpsForEvent(anyLong(), anyLong());
    }

    @Test
    void newEventForm_returnsFormWithEmptyRequest() throws Exception {
        mockMvc.perform(get("/events/new").with(user(currentUser(1L, Role.USER))))
                .andExpect(status().isOk())
                .andExpect(view().name("event_form"))
                .andExpect(model().attribute("isEdit", false))
                .andExpect(model().attributeExists("eventForm"));
    }

    @Test
    void editEventForm_organizerCanAccess() throws Exception {
        Event event = event(4L, 1L, LocalDateTime.of(2026, 2, 15, 18, 0));
        when(eventService.getEventById(4L)).thenReturn(event);

        mockMvc.perform(get("/events/{id}/edit", 4L).with(user(currentUser(1L, Role.USER))))
                .andExpect(status().isOk())
                .andExpect(view().name("event_form"))
                .andExpect(model().attribute("isEdit", true))
                .andExpect(model().attribute("eventId", 4L));
    }

    @Test
    void editEventForm_nonOrganizerThrowsUnauthorized() throws Exception {
        Event event = event(4L, 1L, LocalDateTime.of(2026, 2, 15, 18, 0));
        when(eventService.getEventById(4L)).thenReturn(event);

        mockMvc.perform(get("/events/{id}/edit", 4L).with(user(currentUser(2L, Role.USER))))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Only the organizer can edit this event"));
    }

    @Test
    void createEvent_validSubmission_redirectsToEvent() throws Exception {
        Event created = event(5L, 1L, LocalDateTime.of(2026, 4, 1, 18, 0));
        when(eventService.createEvent(any(EventRequest.class), eq(1L))).thenReturn(created);

        mockMvc.perform(post("/events")
                        .param("title", "New Event")
                        .param("description", "Description")
                        .param("location", "Riga")
                        .param("eventDateTime", futureDateTimeParam())
                        .param("capacity", "50")
                        .param("category", "MUSIC")
                        .with(user(currentUser(1L, Role.USER)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/5"));

        verify(eventService).createEvent(any(EventRequest.class), eq(1L));
    }

    @Test
    void createEvent_validationErrors_reRendersForm() throws Exception {
        mockMvc.perform(post("/events")
                        .param("title", "")
                        .param("description", "Description")
                        .param("location", "Riga")
                        .param("eventDateTime", futureDateTimeParam())
                        .param("capacity", "50")
                        .param("category", "MUSIC")
                        .with(user(currentUser(1L, Role.USER)))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("event_form"))
                .andExpect(model().attribute("isEdit", false));

        verifyNoInteractions(eventService);
    }

    @Test
    void updateEvent_validSubmission_redirectsToEvent() throws Exception {
        Event updated = event(4L, 1L, LocalDateTime.of(2026, 4, 1, 18, 0));
        when(eventService.updateEvent(eq(4L), any(EventRequest.class), eq(1L))).thenReturn(updated);

        mockMvc.perform(put("/events/{id}", 4L)
                        .param("title", "Updated Event")
                        .param("description", "Description")
                        .param("location", "Riga")
                        .param("eventDateTime", futureDateTimeParam())
                        .param("capacity", "50")
                        .param("category", "MUSIC")
                        .with(user(currentUser(1L, Role.USER)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/4"));

        verify(eventService).updateEvent(eq(4L), any(EventRequest.class), eq(1L));
    }

    @Test
    void updateEvent_validationErrors_reRendersForm() throws Exception {
        mockMvc.perform(put("/events/{id}", 4L)
                        .param("title", "")
                        .param("description", "Description")
                        .param("location", "Riga")
                        .param("eventDateTime", futureDateTimeParam())
                        .param("capacity", "50")
                        .param("category", "MUSIC")
                        .with(user(currentUser(1L, Role.USER)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("event_form"))
                .andExpect(model().attribute("isEdit", true))
                .andExpect(model().attribute("eventId", 4L));

        verifyNoInteractions(eventService);
    }

    @Test
    void deleteEvent_callsServiceAndRedirects() throws Exception {
        mockMvc.perform(delete("/events/{id}", 4L)
                        .with(user(currentUser(1L, Role.USER)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"));

        verify(eventService).deleteEvent(4L, 1L);
    }
}
