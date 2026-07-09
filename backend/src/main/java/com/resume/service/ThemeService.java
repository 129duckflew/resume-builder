package com.resume.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.stream.Collectors;

@Service
public class ThemeService {

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

        // Load variables from theme.json
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
        } catch (Exception e) {
            // theme.json is optional; leave variablesSchema null
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

    public Optional<Theme> findById(String id) {
        return themeRepository.findById(id);
    }
}
