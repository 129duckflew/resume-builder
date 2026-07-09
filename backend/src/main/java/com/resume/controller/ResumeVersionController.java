package com.resume.controller;

import com.resume.dto.VersionDiffResponse;
import com.resume.entity.Resume;
import com.resume.entity.ResumeVersion;
import com.resume.service.ResumeService;
import com.resume.service.ResumeVersionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resumes/{resumeId}/versions")
public class ResumeVersionController {

    private final ResumeVersionService versionService;
    private final ResumeService resumeService;

    public ResumeVersionController(ResumeVersionService versionService, ResumeService resumeService) {
        this.versionService = versionService;
        this.resumeService = resumeService;
    }

    @GetMapping
    public List<ResumeVersion> list(@PathVariable String resumeId) {
        resumeService.findByIdAndUserId(resumeId, currentUserId())
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        return versionService.getVersions(resumeId);
    }

    @GetMapping("/{version}")
    public ResumeVersion get(@PathVariable String resumeId, @PathVariable int version) {
        resumeService.findByIdAndUserId(resumeId, currentUserId())
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        return versionService.getVersion(resumeId, version);
    }

    @GetMapping("/diff")
    public ResponseEntity<?> diff(@PathVariable String resumeId,
                                   @RequestParam int a,
                                   @RequestParam int b) {
        if (a < 1 || b < 1) {
            return ResponseEntity.badRequest().body(Map.of("error", "Version numbers must be >= 1"));
        }
        if (resumeService.findByIdAndUserId(resumeId, currentUserId()).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (a == b) {
            return ResponseEntity.badRequest().body("a and b must be different versions");
        }
        VersionDiffResponse response = versionService.getDiff(resumeId, a, b);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{version}/restore")
    public Resume restore(@PathVariable String resumeId, @PathVariable int version) {
        Long userId = currentUserId();
        resumeService.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        Resume restored = versionService.restoreVersion(resumeId, version);
        return resumeService.restoreFromVersion(restored, userId);
    }

    private Long currentUserId() {
        String principal = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return Long.parseLong(principal.split(":", 2)[0]);
    }
}
