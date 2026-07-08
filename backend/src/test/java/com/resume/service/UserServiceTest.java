package com.resume.service;

import com.resume.dto.RegisterRequest;
import com.resume.entity.User;
import com.resume.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void register_createsUser() {
        var req = new RegisterRequest();
        req.setUsername("alice");
        req.setEmail("alice@test.com");
        req.setPassword("secret");

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("$2a$10$encoded");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        User result = service.register(req);

        assertEquals("alice", result.getUsername());
        assertEquals("alice@test.com", result.getEmail());
        assertEquals("$2a$10$encoded", result.getPassword());
    }

    @Test
    void register_withDuplicateUsername_throws() {
        var req = new RegisterRequest();
        req.setUsername("dup");
        req.setEmail("dup@test.com");
        req.setPassword("secret");

        when(userRepository.existsByUsername("dup")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> service.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    void loadByUsername_returnsUser() {
        var user = new User();
        user.setId(1L);
        user.setUsername("alice");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        User result = service.loadByUsername("alice");
        assertEquals("alice", result.getUsername());
    }

    @Test
    void loadByUsername_withMissingUser_throws() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.loadByUsername("missing"));
    }
}
