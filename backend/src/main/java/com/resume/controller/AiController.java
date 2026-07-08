package com.resume.controller;

import com.resume.entity.Resume;
import com.resume.service.AiService;
import com.resume.service.ResumeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/resumes/{resumeId}/ai")
public class AiController {

    private final AiService aiService;
    private final ResumeService resumeService;

    public AiController(AiService aiService, ResumeService resumeService) {
        this.aiService = aiService;
        this.resumeService = resumeService;
    }

    @PostMapping("/rewrite")
    public ResponseEntity<Map<String, String>> rewrite(
            @PathVariable String resumeId,
            @RequestBody Map<String, String> body) {
        Long userId = currentUserId();
        Resume resume = resumeService.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        String instruction = body.getOrDefault("instruction", "Improve the writing");
        String result = aiService.rewrite(resume, userId, instruction);
        return ResponseEntity.ok(Map.of("content", result));
    }

    @PostMapping("/suggest")
    public ResponseEntity<Map<String, String>> suggest(
            @PathVariable String resumeId,
            @RequestBody Map<String, String> body) {
        Long userId = currentUserId();
        Resume resume = resumeService.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        String jobDescription = body.getOrDefault("jobDescription", "");
        String result = aiService.suggest(resume, userId, jobDescription);
        return ResponseEntity.ok(Map.of("content", result));
    }

    private Long currentUserId() {
        String principal = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return Long.parseLong(principal.split(":", 2)[0]);
    }
}
