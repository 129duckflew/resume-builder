package com.resume.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.dto.ThemeDTO;
import com.resume.dto.VariableDeclaration;
import com.resume.entity.Theme;
import com.resume.repository.ThemeRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ThemeService {

    private static final Set<String> VALID_LAYOUTS = Set.of(
            "single", "sidebar-left", "sidebar-right", "header-bar");

    private final ThemeRepository themeRepository;
    private final ResourceLoader resourceLoader;

    @Value("${app.themes.path:classpath:themes/}")
    private String themesPath;

    public ThemeService(ThemeRepository themeRepository, ResourceLoader resourceLoader) {
        this.themeRepository = themeRepository;
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void initBuiltInThemes() {
        loadOrRefreshBuiltIn("classic", "Classic", "Traditional corporate style with serif fonts, monochrome");
        loadOrRefreshBuiltIn("modern", "Modern", "Clean sans-serif style for tech companies");
        loadOrRefreshBuiltIn("minimal", "Minimal", "Ultra-minimalist style for academic/research");
        loadOrRefreshBuiltIn("sidebar", "Sidebar", "Two-column layout with colored sidebar for contact and skills");
        loadOrRefreshBuiltIn("stackoverflow", "Stack Overflow", "Developer-friendly style with tag-like skills");
        loadOrRefreshBuiltIn("elegant", "Elegant", "Refined business style with warm tones and serif typography");
        loadOrRefreshBuiltIn("compact", "Compact", "Dense layout for experienced professionals");
        loadOrRefreshBuiltIn("sidebar-right", "Sidebar Right", "Two-column layout with colored sidebar on the right");
        loadOrRefreshBuiltIn("header-bar", "Header Bar", "Top header bar with colored background");
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    private void loadOrRefreshBuiltIn(String id, String name, String description) {
        Theme theme = themeRepository.findById(id).orElse(new Theme());
        theme.setId(id);
        theme.setName(name);
        theme.setDescription(description);
        theme.setBuiltIn(true);

        try {
            Resource resource = resourceLoader.getResource(themesPath + id + "/style.css");
            String css = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
            theme.setCssContent(css);
        } catch (Exception e) {
            theme.setCssContent("/* Theme: " + id + " */");
        }

        // Load variables and layout from theme.json
        try {
            Resource jsonResource = resourceLoader.getResource(themesPath + id + "/theme.json");
            String json = new BufferedReader(
                    new InputStreamReader(jsonResource.getInputStream(), StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
            Map<String, Object> themeJson = objectMapper.readValue(json,
                    new TypeReference<Map<String, Object>>() {});
            Object variables = themeJson.get("variables");
            if (variables != null) {
                String variablesJson = objectMapper.writeValueAsString(variables);
                theme.setVariablesSchema(variablesJson);
            }
            Object layout = themeJson.get("layout");
            if (layout != null) {
                theme.setLayout(layout.toString());
            } else {
                theme.setLayout("single");
            }
        } catch (Exception e) {
            // theme.json is optional; leave defaults
        }

        themeRepository.save(theme);
    }

    public List<VariableDeclaration> getVariables(String themeId) {
        return findById(themeId)
                .map(theme -> {
                    String schema = theme.getVariablesSchema();
                    if (schema == null) return Collections.<VariableDeclaration>emptyList();
                    try {
                        return objectMapper.readValue(schema,
                                new TypeReference<List<VariableDeclaration>>() {});
                    } catch (Exception e) {
                        return Collections.<VariableDeclaration>emptyList();
                    }
                })
                .orElse(null);
    }

    public List<Theme> findAll() {
        return themeRepository.findAllByOrderBySortOrderAsc();
    }

    public List<Theme> findAll(Long userId) {
        if (userId == null) {
            return themeRepository.findByBuiltInTrueOrderBySortOrderAsc();
        }
        return themeRepository.findByBuiltInTrueOrUserIdOrderBySortOrderAsc(userId);
    }

    public Optional<Theme> findById(String id) {
        return themeRepository.findById(id);
    }

    public Optional<Theme> findByIdAndUserId(String id, Long userId) {
        return themeRepository.findByIdAndUserId(id, userId);
    }

    public Theme createCustomTheme(ThemeDTO dto, Long userId) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Theme name is required");
        }
        String layout = dto.getLayout() != null ? dto.getLayout() : "single";
        if (!VALID_LAYOUTS.contains(layout)) {
            throw new IllegalArgumentException("Invalid layout: " + layout);
        }
        String id = "user-" + userId + "-" + UUID.randomUUID().toString().substring(0, 8);
        Theme theme = new Theme();
        theme.setId(id);
        theme.setName(dto.getName().trim());
        theme.setDescription(dto.getDescription());
        theme.setBuiltIn(false);
        theme.setUserId(userId);
        theme.setLayout(layout);
        String css = dto.getCssContent();
        if (css != null) {
            theme.setCssContent(CssSanitizer.sanitize(css));
        }
        if (dto.getVariablesSchema() != null) {
            theme.setVariablesSchema(dto.getVariablesSchema());
        }
        return themeRepository.save(theme);
    }

    public Theme updateCustom(String id, ThemeDTO dto, Long userId) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Theme not found: " + id));
        if (theme.isBuiltIn()) {
            throw new IllegalStateException("Cannot modify a built-in theme");
        }
        if (!userId.equals(theme.getUserId())) {
            throw new SecurityException("Not authorized to modify this theme");
        }
        if (dto.getName() != null) {
            theme.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null) {
            theme.setDescription(dto.getDescription());
        }
        if (dto.getLayout() != null) {
            if (!VALID_LAYOUTS.contains(dto.getLayout())) {
                throw new IllegalArgumentException("Invalid layout: " + dto.getLayout());
            }
            theme.setLayout(dto.getLayout());
        }
        if (dto.getCssContent() != null) {
            theme.setCssContent(CssSanitizer.sanitize(dto.getCssContent()));
        }
        if (dto.getVariablesSchema() != null) {
            theme.setVariablesSchema(dto.getVariablesSchema());
        }
        return themeRepository.save(theme);
    }

    public void deleteCustom(String id, Long userId) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Theme not found: " + id));
        if (theme.isBuiltIn()) {
            throw new IllegalStateException("Cannot delete a built-in theme");
        }
        if (!userId.equals(theme.getUserId())) {
            throw new SecurityException("Not authorized to delete this theme");
        }
        themeRepository.delete(theme);
    }
}
