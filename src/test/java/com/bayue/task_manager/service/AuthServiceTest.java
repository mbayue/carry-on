package com.bayue.task_manager.service;

import com.bayue.task_manager.dto.request.LoginRequest;
import com.bayue.task_manager.dto.request.RegisterRequest;
import com.bayue.task_manager.dto.response.AuthResponse;
import com.bayue.task_manager.entity.Role;
import com.bayue.task_manager.entity.User;
import com.bayue.task_manager.exception.UserAlreadyExistsException;
import com.bayue.task_manager.repository.UserRepository;
import com.bayue.task_manager.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
            .id(UUID.randomUUID())
            .name("Test User")
            .email("test@example.com")
            .password("hashedPassword")
            .role(Role.USER)
            .build();
    }

    @Test
    @DisplayName("register: Success when email is unique")
    void register_Success() {
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(tokenProvider.generateToken(sampleUser.getId(), "test@example.com", "USER")).thenReturn("jwt.token.here");

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt.token.here");
        verify(cacheService, times(1)).storeActiveToken(eq("jwt.token.here"), eq(Duration.ofHours(1)));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("register: Throws UserAlreadyExistsException when email is taken")
    void register_DuplicateEmail_ThrowsException() {
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(UserAlreadyExistsException.class)
            .hasMessage("User already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("login: Success with valid credentials")
    void login_Success() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(tokenProvider.generateToken(sampleUser.getId(), "test@example.com", "USER")).thenReturn("jwt.token.here");

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt.token.here");
        verify(cacheService, times(1)).storeActiveToken(eq("jwt.token.here"), eq(Duration.ofHours(1)));
    }

    @Test
    @DisplayName("login: Throws BadCredentialsException when user not found")
    void login_UserNotFound_ThrowsBadCredentials() {
        LoginRequest request = new LoginRequest("unknown@example.com", "password123");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessage("Invalid credentials");
    }

    @Test
    @DisplayName("login: Throws BadCredentialsException when password does not match")
    void login_WrongPassword_ThrowsBadCredentials() {
        LoginRequest request = new LoginRequest("test@example.com", "wrongPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessage("Invalid credentials");
    }

    @Test
    @DisplayName("logout: Revokes token in Redis when Bearer token is provided")
    void logout_Success() {
        authService.logout("Bearer my.secret.token");

        verify(cacheService, times(1)).revokeToken("my.secret.token");
    }

    @Test
    @DisplayName("logout: Does nothing when Authorization header is invalid or missing")
    void logout_InvalidHeader_NoOp() {
        authService.logout(null);
        authService.logout("Basic something");

        verify(cacheService, never()).revokeToken(anyString());
    }
}
