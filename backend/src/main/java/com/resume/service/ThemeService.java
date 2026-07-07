package com.resume.service;

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
import java.util.List;
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
        loadThemeIfAbsent("classic", "Classic", "Traditional corporate style with serif fonts, monochrome");
        loadThemeIfAbsent("modern", "Modern", "Clean sans-serif style for tech companies");
        loadThemeIfAbsent("minimal", "Minimal", "Ultra-minimalist style for academic/research");
    }

    private void loadThemeIfAbsent(String id, String name, String description) {
        if (themeRepository.findById(id).isPresent()) return;

        Theme theme = new Theme();
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

        themeRepository.save(theme);
    }

    public List<Theme> findAll() {
        return themeRepository.findAllByOrderBySortOrderAsc();
    }

    public Optional<Theme> findById(String id) {
        return themeRepository.findById(id);
    }
}
