package com.orbit.team.controller.page;

import com.orbit.team.entity.Role;
import com.orbit.team.entity.User;
import com.orbit.team.security.UserSecurity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HomePageController.class)
class HomePageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private UserSecurity anyAuthenticatedUser() {
        User user = User.builder()
                .id(1L)
                .username("user")
                .email("user@example.com")
                .password("password")
                .fullName("Full Name")
                .role(Role.USER)
                .active(true)
                .build();
        return new UserSecurity(user);
    }

    @Test
    void home_redirectsToEvents() throws Exception {
        mockMvc.perform(get("/home").with(user(anyAuthenticatedUser())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"));
    }

    @Test
    void root_redirectsToEvents() throws Exception {
        mockMvc.perform(get("/").with(user(anyAuthenticatedUser())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"));
    }
}
