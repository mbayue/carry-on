package com.bayue.task_manager.service;

import com.bayue.task_manager.dto.request.LoginRequest;
import com.bayue.task_manager.dto.request.RegisterRequest;
import com.bayue.task_manager.dto.response.AuthResponse;
import com.bayue.task_manager.entity.Role;
import com.bayue.task_manager.entity.User;
import com.bayue.task_manager.exception.UserAlreadyExistsException;
import com.bayue.task_manager.repository.UserRepository;
import com.bayue.task_manager.security.JwtTokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final CacheService cacheService;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtTokenProvider tokenProvider,
        CacheService cacheService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.cacheService = cacheService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("User already exists");
        }

        User user = User.builder()
            .name(request.name())
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .role(Role.USER)
            .build();

        User savedUser = userRepository.save(user);
        String token = tokenProvider.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name());
        cacheService.storeActiveToken(token, Duration.ofHours(1));

        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        cacheService.storeActiveToken(token, Duration.ofHours(1));

        return new AuthResponse(token);
    }

    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            cacheService.revokeToken(token);
        }
    }
}
