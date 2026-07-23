package com.resume.controller;

import tools.jackson.databind.ObjectMapper;
import com.resume.config.JwtUtil;
import com.resume.dto.LoginRequest;
import com.resume.dto.RegisterRequest;
import com.resume.entity.User;
import com.resume.service.ResumeService;
import com.resume.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private ResumeService resumeService;

    @Test
    void register_createsUserAndReturnsToken() throws Exception {
        var req = new RegisterRequest();
        req.setUsername("newuser");
        req.setEmail("new@test.com");
        req.setPassword("secret123");

        var user = new User();
        user.setId(1L);
        user.setUsername("newuser");

        when(userService.register(any())).thenReturn(user);
        when(jwtUtil.generateToken(1L, "newuser")).thenReturn("mock-token");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-token"))
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    void register_withDuplicateUsername_returns400() throws Exception {
        var req = new RegisterRequest();
        req.setUsername("dup");
        req.setEmail("dup@test.com");
        req.setPassword("secret123");

        when(userService.register(any())).thenThrow(new RuntimeException("Username already taken"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username already taken"));
    }

    @Test
    void login_withValidCredentials_returnsToken() throws Exception {
        var req = new LoginRequest();
        req.setUsername("testuser");
        req.setPassword("correct");

        var user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("$2a$10$encoded");

        when(userService.loadByUsername("testuser")).thenReturn(user);
        when(passwordEncoder.matches("correct", "$2a$10$encoded")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "testuser")).thenReturn("mock-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-token"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void login_withInvalidPassword_returns401() throws Exception {
        var req = new LoginRequest();
        req.setUsername("testuser");
        req.setPassword("wrong");

        var user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("$2a$10$encoded");

        when(userService.loadByUsername("testuser")).thenReturn(user);
        when(passwordEncoder.matches("wrong", "$2a$10$encoded")).thenReturn(false);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }
}
