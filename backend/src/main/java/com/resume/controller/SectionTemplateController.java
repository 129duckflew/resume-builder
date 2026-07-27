package com.resume.controller;

import com.resume.entity.SectionTemplate;
import com.resume.service.SectionTemplateService;
import org.springframework.http.ResponseEntity;
import com.resume.config.CurrentUserId;
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
    public List<SectionTemplate> list(@CurrentUserId Long userId) {
        return service.getEffectiveTemplates(userId);
    }

    @PostMapping
    public SectionTemplate create(@RequestBody SectionTemplate template, @CurrentUserId Long userId) {
        return service.create(template, userId);
    }

    @PutMapping("/{id}")
    public SectionTemplate update(@PathVariable Long id, @RequestBody SectionTemplate template,
                                    @CurrentUserId Long userId) {
        return service.update(id, template, userId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @CurrentUserId Long userId) {
        service.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
