package com.bayue.task_manager.controller;

import com.bayue.task_manager.dto.request.LoginRequest;
import com.bayue.task_manager.dto.request.RegisterRequest;
import com.bayue.task_manager.dto.response.AuthResponse;
import com.bayue.task_manager.exception.GlobalExceptionHandler;
import com.bayue.task_manager.exception.UserAlreadyExistsException;
import com.bayue.task_manager.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    @DisplayName("POST /api/auth/register: Returns 201 Created on valid request")
    void register_Success() throws Exception {
        RegisterRequest request = new RegisterRequest("Alice", "alice@example.com", "password123");
        when(authService.register(any(RegisterRequest.class))).thenReturn(new AuthResponse("jwt.mock.token"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.code").value(201))
            .andExpect(jsonPath("$.message").value("User registered successfully"))
            .andExpect(jsonPath("$.data.token").value("jwt.mock.token"));
    }

    @Test
    @DisplayName("POST /api/auth/register: Returns 400 Bad Request on invalid input")
    void register_ValidationError() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("", "not-an-email", "short");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("bad_request"))
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("POST /api/auth/register: Returns 409 Conflict when user already exists")
    void register_DuplicateEmail_Conflict() throws Exception {
        RegisterRequest request = new RegisterRequest("Alice", "alice@example.com", "password123");
        when(authService.register(any(RegisterRequest.class)))
            .thenThrow(new UserAlreadyExistsException("User already exists"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value("conflict"))
            .andExpect(jsonPath("$.code").value(409))
            .andExpect(jsonPath("$.message").value("User already exists"));
    }

    @Test
    @DisplayName("POST /api/auth/login: Returns 200 OK with token on valid credentials")
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest("alice@example.com", "password123");
        when(authService.login(any(LoginRequest.class))).thenReturn(new AuthResponse("jwt.mock.token"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.token").value("jwt.mock.token"));
    }

    @Test
    @DisplayName("POST /api/auth/login: Returns 401 Unauthorized on bad credentials")
    void login_BadCredentials() throws Exception {
        LoginRequest request = new LoginRequest("alice@example.com", "wrongPassword");
        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value("unauthorized"))
            .andExpect(jsonPath("$.code").value(401))
            .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    @DisplayName("POST /api/auth/logout: Returns 200 OK on logout")
    void logout_Success() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer valid.token.here"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("Logged out successfully"));

        verify(authService, times(1)).logout("Bearer valid.token.here");
    }
}
