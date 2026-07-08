package com.resume.controller;

import com.resume.dto.DesensitizeRuleDTO;
import com.resume.service.DesensitizeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class DesensitizeController {

    private final DesensitizeService desensitizeService;

    public DesensitizeController(DesensitizeService desensitizeService) {
        this.desensitizeService = desensitizeService;
    }

    @GetMapping("/desensitize-rules")
    public List<DesensitizeRuleDTO> getRules() {
        return desensitizeService.getEffectiveRules(currentUserId());
    }

    @PutMapping("/desensitize-rules")
    public ResponseEntity<Void> saveRules(@RequestBody List<DesensitizeRuleDTO> rules) {
        desensitizeService.saveUserRules(currentUserId(), rules);
        return ResponseEntity.ok().build();
    }

    private Long currentUserId() {
        String principal = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return Long.parseLong(principal.split(":", 2)[0]);
    }
}
