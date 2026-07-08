package com.resume.controller;

import com.resume.entity.SectionTemplate;
import com.resume.service.SectionTemplateService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/section-templates")
public class SectionTemplateController {

    private final SectionTemplateService service;

    public SectionTemplateController(SectionTemplateService service) {
        this.service = service;
    }

    @GetMapping
    public List<SectionTemplate> list() {
        return service.getEffectiveTemplates(currentUserId());
    }

    @PostMapping
    public SectionTemplate create(@RequestBody SectionTemplate template) {
        return service.create(template, currentUserId());
    }

    @PutMapping("/{id}")
    public SectionTemplate update(@PathVariable Long id, @RequestBody SectionTemplate template) {
        return service.update(id, template, currentUserId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId() {
        String principal = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return Long.parseLong(principal.split(":", 2)[0]);
    }
}
