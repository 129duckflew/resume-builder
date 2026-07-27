package com.resume.controller;

import com.resume.service.UserService;
import org.springframework.http.ResponseEntity;
import com.resume.config.CurrentUserId;
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
    public ResponseEntity<Map<String, String>> getApiKey(@CurrentUserId Long userId) {
        String key = userService.getApiKey(userId);
        return ResponseEntity.ok(Map.of("apiKey", key != null ? key : ""));
    }

    @PutMapping("/api-key")
    public ResponseEntity<Void> updateApiKey(@RequestBody Map<String, String> body,
                                               @CurrentUserId Long userId) {
        userService.updateApiKey(userId, body.getOrDefault("apiKey", ""));
        return ResponseEntity.ok().build();
    }
}
