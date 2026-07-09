package com.resume.controller;

import com.resume.dto.ThemeDTO;
import com.resume.dto.VariableDeclaration;
import com.resume.entity.Theme;
import com.resume.service.ThemeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        Long userId = getCurrentUserId();
        return themeService.findAll(userId);
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

    @GetMapping("/{id}/variables")
    public ResponseEntity<List<VariableDeclaration>> getVariables(@PathVariable String id) {
        List<VariableDeclaration> vars = themeService.getVariables(id);
        if (vars == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(vars);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ThemeDTO dto) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Theme theme = themeService.createCustomTheme(dto, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(theme);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody ThemeDTO dto) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Theme theme = themeService.updateCustom(id, dto, userId);
            return ResponseEntity.ok(theme);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            themeService.deleteCustom(id, userId);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            String p = (String) principal;
            try {
                return Long.parseLong(p.split(":", 2)[0]);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
