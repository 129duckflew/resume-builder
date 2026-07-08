package com.resume.controller;

import com.resume.config.JwtUtil;
import com.resume.dto.AuthResponse;
import com.resume.dto.LoginRequest;
import com.resume.dto.RegisterRequest;
import com.resume.service.ResumeService;
import com.resume.service.UserService;
import jakarta.validation.Valid;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final ResumeService resumeService;

    public AuthController(UserService userService, JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder, ResumeService resumeService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.resumeService = resumeService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        try {
            var user = userService.register(req);
            resumeService.assignOrphanResumes(user.getId());
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());
            return ResponseEntity.ok(new AuthResponse(token, user.getUsername()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            var user = userService.loadByUsername(req.getUsername());
            if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid username or password"));
            }
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());
            return ResponseEntity.ok(new AuthResponse(token, user.getUsername()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }
    }
}
