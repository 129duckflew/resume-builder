package com.resume.controller;

import com.resume.dto.DesensitizeRuleDTO;
import com.resume.service.DesensitizeService;
import org.springframework.http.ResponseEntity;
import com.resume.config.CurrentUserId;
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
    public List<DesensitizeRuleDTO> getRules(@CurrentUserId Long userId) {
        return desensitizeService.getEffectiveRules(userId);
    }

    @PutMapping("/desensitize-rules")
    public ResponseEntity<Void> saveRules(@RequestBody List<DesensitizeRuleDTO> rules,
                                           @CurrentUserId Long userId) {
        desensitizeService.saveUserRules(userId, rules);
        return ResponseEntity.ok().build();
    }
}
