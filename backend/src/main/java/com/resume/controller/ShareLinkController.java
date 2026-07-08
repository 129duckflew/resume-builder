package com.resume.controller;

import com.resume.entity.Resume;
import com.resume.entity.ShareLink;
import com.resume.service.ExportService;
import com.resume.service.ResumeService;
import com.resume.service.ShareLinkService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class ShareLinkController {

    private final ShareLinkService shareLinkService;
    private final ResumeService resumeService;
    private final ExportService exportService;

    public ShareLinkController(ShareLinkService shareLinkService,
                               ResumeService resumeService,
                               ExportService exportService) {
        this.shareLinkService = shareLinkService;
        this.resumeService = resumeService;
        this.exportService = exportService;
    }

    // Authenticated: manage share links for own resume
    @GetMapping("/api/resumes/{resumeId}/shares")
    public List<ShareLink> list(@PathVariable String resumeId) {
        resumeService.findByIdAndUserId(resumeId, currentUserId())
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        return shareLinkService.getLinks(resumeId);
    }

    @PostMapping("/api/resumes/{resumeId}/shares")
    public ShareLink create(@PathVariable String resumeId,
                            @RequestBody(required = false) Map<String, Boolean> body) {
        resumeService.findByIdAndUserId(resumeId, currentUserId())
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        boolean desensitize = body != null && Boolean.TRUE.equals(body.get("desensitize"));
        return shareLinkService.createLink(resumeId, desensitize);
    }

    @DeleteMapping("/api/shares/{linkId}")
    public ResponseEntity<Void> delete(@PathVariable String linkId) {
        shareLinkService.deleteLink(linkId);
        return ResponseEntity.noContent().build();
    }

    // Public: view shared resume
    @GetMapping("/s/{token}")
    public ResponseEntity<String> viewPublic(@PathVariable String token) {
        ShareLink link = shareLinkService.getPublicLink(token);
        Resume resume = resumeService.findById(link.getResumeId())
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        String html = exportService.generateHtml(resume,
                Boolean.TRUE.equals(link.getDesensitize()),
                resume.getUserId());
        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=utf-8")
                .body(html);
    }

    private Long currentUserId() {
        String principal = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return Long.parseLong(principal.split(":", 2)[0]);
    }
}
