package com.resume.controller;

import com.resume.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserSettingsController {

    private final UserService userService;

    public UserSettingsController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api-key")
    public ResponseEntity<Map<String, String>> getApiKey() {
        String key = userService.getApiKey(currentUserId());
        return ResponseEntity.ok(Map.of("apiKey", key != null ? key : ""));
    }

    @PutMapping("/api-key")
    public ResponseEntity<Void> updateApiKey(@RequestBody Map<String, String> body) {
        userService.updateApiKey(currentUserId(), body.getOrDefault("apiKey", ""));
        return ResponseEntity.ok().build();
    }

    private Long currentUserId() {
        String principal = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return Long.parseLong(principal.split(":", 2)[0]);
    }
}
