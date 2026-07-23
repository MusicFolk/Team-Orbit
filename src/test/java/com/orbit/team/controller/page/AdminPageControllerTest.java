package com.orbit.team.controller.page;

import com.orbit.team.dto.response.UserResponse;
import com.orbit.team.entity.Comment;
import com.orbit.team.entity.Event;
import com.orbit.team.entity.EventCategory;
import com.orbit.team.entity.Role;
import com.orbit.team.entity.User;
import com.orbit.team.security.UserSecurity;
import com.orbit.team.service.AdminService;
import com.orbit.team.service.CommentService;
import com.orbit.team.service.EventService;
import com.orbit.team.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AdminPageController.class)
public class AdminPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CommentService commentService;

    private UserSecurity currentUser(long id, Role role) {
        User user = User.builder()
                .id(id)
                .username("admin")
                .email("admin@example.com")
                .password("password")
                .fullName("Admin User")
                .role(role)
                .active(true)
                .build();
        return new UserSecurity(user);
    }

    private User createUser(long id, String fullName, String username, String email) {
        return User.builder()
                .id(id)
                .fullName(fullName)
                .username(username)
                .email(email)
                .role(Role.USER)
                .active(true)
                .build();
    }

    private UserResponse userResponse(long id, boolean active) {
        UserResponse response = new UserResponse();
        response.setUserId(id);
        response.setUsername("alice");
        response.setEmail("alice@example.com");
        response.setFullName("Alice Smith");
        response.setActive(active);
        return response;
    }

    private Event event(long id, String title, User organizer) {
        return Event.builder()
                .id(id)
                .title(title)
                .description("Description")
                .location("Riga")
                .category(EventCategory.MUSIC)
                .eventDateTime(LocalDateTime.of(2026, 5, 1, 18, 0))
                .capacity(50)
                .organizer(organizer)
                .build();
    }

    private Comment comment(long id, String content, User author, Event event) {
        return Comment.builder()
                .id(id)
                .content(content)
                .user(author)
                .event(event)
                .build();
    }

    @Test
    void users_noSearch_returnsAllUsers() throws Exception {
        User alice = createUser(1L, "Alice Smith", "alice", "alice@example.com");
        User bob = createUser(2L, "Bob Jones", "bob", "bob@example.com");

        when(adminService.getAllUsers()).thenReturn(List.of(alice, bob));

        mockMvc.perform(get("/admin/users").with(user(currentUser(9L, Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(view().name("admin_users"))
                .andExpect(model().attribute("users", hasSize(2)));
    }

    @Test
    void users_withSearch_filtersByFullNameUsernameOrEmail() throws Exception {
        User alice = createUser(1L, "Alice Smith", "alice", "alice@example.com");
        User bob = createUser(2L, "Bob Jones", "bob", "bob@example.com");

        when(adminService.getAllUsers()).thenReturn(List.of(alice, bob));

        mockMvc.perform(get("/admin/users")
                        .param("search", "Alice")
                        .with(user(currentUser(9L, Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(view().name("admin_users"))
                .andExpect(model().attribute("users", hasSize(1)));
    }

    @Test
    void toggleUserStatus_activeUser_deactivatesAndRedirects() throws Exception {
        when(userService.getById(1L)).thenReturn(userResponse(1L, true));

        mockMvc.perform(patch("/admin/users/{id}/status", 1L)
                        .with(user(currentUser(9L, Role.ADMIN)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userService).setStatus(1L, false);
    }

    @Test
    void toggleUserStatus_inactiveUser_activatesAndRedirects() throws Exception {
        when(userService.getById(1L)).thenReturn(userResponse(1L, false));

        mockMvc.perform(patch("/admin/users/{id}/status", 1L)
                        .with(user(currentUser(9L, Role.ADMIN)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userService).setStatus(1L, true);
    }

    @Test
    void events_noSearch_returnsAllEvents() throws Exception {
        User organizer = createUser(1L, "Alice Smith", "alice", "alice@example.com");
        Event event1 = event(1L, "Jazz Night", organizer);
        Event event2 = event(2L, "Rock Show", organizer);

        when(adminService.getAllEvents()).thenReturn(List.of(event1, event2));

        mockMvc.perform(get("/admin/events").with(user(currentUser(9L, Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(view().name("admin_events"))
                .andExpect(model().attribute("events", hasSize(2)));
    }

    @Test
    void events_withSearch_filtersByTitleOrOrganizer() throws Exception {
        User organizer = createUser(1L, "Alice Smith", "alice", "alice@example.com");
        Event event1 = event(1L, "Jazz Night", organizer);
        Event event2 = event(2L, "Rock Show", organizer);

        when(adminService.getAllEvents()).thenReturn(List.of(event1, event2));

        mockMvc.perform(get("/admin/events")
                        .param("search", "Jazz")
                        .with(user(currentUser(9L, Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(view().name("admin_events"))
                .andExpect(model().attribute("events", hasSize(1)));
    }

    @Test
    void deleteEvent_callsEventServiceWithAdminIdAndRedirects() throws Exception {
        mockMvc.perform(delete("/admin/events/{id}", 1L)
                        .with(user(currentUser(9L, Role.ADMIN)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/events"));

        verify(eventService).deleteEvent(eq(1L), eq(9L));
    }

    @Test
    void comments_noFilters_returnsAllComments() throws Exception {
        User author = createUser(1L, "Alice Smith", "alice", "alice@example.com");
        Event event = event(1L, "Jazz Night", author);
        Comment comment1 = comment(1L, "Great event!", author, event);
        Comment comment2 = comment(2L, "Loved it", author, event);

        when(adminService.getAllComments()).thenReturn(List.of(comment1, comment2));

        mockMvc.perform(get("/admin/comments").with(user(currentUser(9L, Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(view().name("admin_comments"))
                .andExpect(model().attribute("comments", hasSize(2)));
    }

    @Test
    void comments_withSearch_filtersByContentAuthorOrEventTitle() throws Exception {
        User author = createUser(1L, "Alice Smith", "alice", "alice@example.com");
        Event event = event(1L, "Jazz Night", author);
        Comment comment1 = comment(1L, "Great event!", author, event);
        Comment comment2 = comment(2L, "Not for me", author, event);

        when(adminService.getAllComments()).thenReturn(List.of(comment1, comment2));

        mockMvc.perform(get("/admin/comments")
                        .param("search", "Great")
                        .with(user(currentUser(9L, Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(view().name("admin_comments"))
                .andExpect(model().attribute("comments", hasSize(1)));
    }

    @Test
    void comments_withUserIdFilter_filtersByAuthor() throws Exception {
        User alice = createUser(1L, "Alice Smith", "alice", "alice@example.com");
        User bob = createUser(2L, "Bob Jones", "bob", "bob@example.com");
        Event event = event(1L, "Jazz Night", alice);
        Comment comment1 = comment(1L, "From Alice", alice, event);
        Comment comment2 = comment(2L, "From Bob", bob, event);

        when(adminService.getAllComments()).thenReturn(List.of(comment1, comment2));

        mockMvc.perform(get("/admin/comments")
                        .param("userId", "1")
                        .with(user(currentUser(9L, Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(view().name("admin_comments"))
                .andExpect(model().attribute("comments", hasSize(1)));
    }

    @Test
    void comments_withEventIdFilter_filtersByEvent() throws Exception {
        User author = createUser(1L, "Alice Smith", "alice", "alice@example.com");
        Event event1 = event(1L, "Jazz Night", author);
        Event event2 = event(2L, "Rock Show", author);
        Comment comment1 = comment(1L, "On Jazz", author, event1);
        Comment comment2 = comment(2L, "On Rock", author, event2);

        when(adminService.getAllComments()).thenReturn(List.of(comment1, comment2));

        mockMvc.perform(get("/admin/comments")
                        .param("eventId", "1")
                        .with(user(currentUser(9L, Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(view().name("admin_comments"))
                .andExpect(model().attribute("comments", hasSize(1)));
    }

    @Test
    void deleteComment_callsCommentServiceWithAdminIdAndRedirects() throws Exception {
        mockMvc.perform(delete("/admin/comments/{id}", 6L)
                        .with(user(currentUser(9L, Role.ADMIN)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/comments"));

        verify(commentService).deleteComment(eq(6L), eq(9L));
    }

    @Test
    void users_blankSearch_returnsAllUsers() throws Exception {
        User alice = createUser(1L, "Alice Smith", "alice", "alice@example.com");
        User bob = createUser(2L, "Bob Jones", "bob", "bob@example.com");

        when(adminService.getAllUsers()).thenReturn(List.of(alice, bob));

        mockMvc.perform(get("/admin/users")
                        .param("search", "")
                        .with(user(currentUser(9L, Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(view().name("admin_users"))
                .andExpect(model().attribute("users", hasSize(2)));
    }

    @Test
    void comments_populatesDistinctUsersAndEvents() throws Exception {
        User alice = createUser(1L, "Alice Smith", "alice", "alice@example.com");
        User bob = createUser(2L, "Bob Jones", "bob", "bob@example.com");

        Event event1 = event(1L, "Jazz Night", alice);
        Event event2 = event(2L, "Rock Show", bob);

        Comment comment1 = comment(1L, "Great", alice, event1);
        Comment comment2 = comment(2L, "Nice", bob, event2);

        when(adminService.getAllComments())
                .thenReturn(List.of(comment1, comment2));

        mockMvc.perform(get("/admin/comments")
                        .with(user(currentUser(9L, Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(view().name("admin_comments"))
                .andExpect(model().attribute("filterUsers", hasSize(2)))
                .andExpect(model().attribute("filterEvents", hasSize(2)));
    }

    @Test
    void comments_withoutSearch_returnsAllComments() throws Exception {
        User author = createUser(1L, "Alice Smith", "alice", "alice@example.com");
        Event event = event(1L, "Jazz Night", author);

        Comment comment1 = comment(1L, "Great event!", author, event);

        when(adminService.getAllComments())
                .thenReturn(List.of(comment1));

        mockMvc.perform(get("/admin/comments")
                        .with(user(currentUser(9L, Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(model().attribute("comments", hasSize(1)));
    }

    @Test
    void users_withBlankSearch_returnsAllUsers() throws Exception {
        User alice = createUser(1L, "Alice Smith", "alice", "alice@example.com");

        when(adminService.getAllUsers()).thenReturn(List.of(alice));

        mockMvc.perform(get("/admin/users")
                        .param("search", "   ")
                        .with(user(currentUser(9L, Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(model().attribute("users", hasSize(1)));
    }

    @Test
    void users_search_handlesNullFields() throws Exception {
        User user = User.builder()
                .id(1L)
                .fullName(null)
                .username(null)
                .email(null)
                .role(Role.USER)
                .active(true)
                .build();

        when(adminService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/admin/users")
                        .param("search", "Alice")
                        .with(user(currentUser(9L, Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(model().attribute("users", hasSize(0)));
    }

    @Test
    void comments_duplicateUsersAndEvents_areDistinct() throws Exception {
        User alice = createUser(1L, "Alice", "alice", "alice@test.com");

        Event event = event(1L, "Jazz", alice);

        Comment comment1 = comment(1L, "First", alice, event);
        Comment comment2 = comment(2L, "Second", alice, event);

        when(adminService.getAllComments()).thenReturn(List.of(comment1, comment2));

        mockMvc.perform(get("/admin/comments")
                        .with(user(currentUser(9L, Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(model().attribute("filterUsers", hasSize(1)))
                .andExpect(model().attribute("filterEvents", hasSize(1)));
    }

    @Test
    void users_withSearch_filtersByUsername() throws Exception {
        User alice = createUser(1L, "Alice Smith", "uniqueUsername", "alice@example.com");

        when(adminService.getAllUsers()).thenReturn(List.of(alice));

        mockMvc.perform(get("/admin/users")
                        .param("search", "uniqueUsername")
                        .with(user(currentUser(9L, Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(model().attribute("users", hasSize(1)));
    }

    @Test
    void comments_withSearch_filtersByAuthorName() throws Exception {
        User alice = createUser(1L, "Unique Author", "alice", "alice@example.com");
        Event event = event(1L, "Jazz Night", alice);
        Comment comment = comment(1L, "Some unrelated text", alice, event);

        when(adminService.getAllComments()).thenReturn(List.of(comment));

        mockMvc.perform(get("/admin/comments")
                        .param("search", "Unique Author")
                        .with(user(currentUser(9L, Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(model().attribute("comments", hasSize(1)));
    }
}
