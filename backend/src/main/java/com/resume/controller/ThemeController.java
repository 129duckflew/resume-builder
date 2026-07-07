package com.resume.controller;

import com.resume.entity.Theme;
import com.resume.service.ThemeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public List<Theme> list() {
        return themeService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Theme> get(@PathVariable String id) {
        return themeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/css")
    public ResponseEntity<String> getCss(@PathVariable String id) {
        return themeService.findById(id)
                .map(theme -> ResponseEntity.ok()
                        .header("Content-Type", "text/css; charset=utf-8")
                        .body(theme.getCssContent()))
                .orElse(ResponseEntity.notFound().build());
    }
}
