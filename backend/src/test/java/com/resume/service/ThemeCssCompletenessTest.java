package com.resume.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ThemeCssCompletenessTest {

    private static final String[] REQUIRED_SELECTORS = {
            "h1 {", "h2 {", "h3 {", "p {", "ul {", "li {", "strong {", "em {", "a {",
            "@page", ".resume-page", "@media print"
    };

    private static final List<String> ALL_THEME_IDS = List.of(
            "classic", "modern", "minimal", "sidebar", "stackoverflow", "elegant", "compact",
            "sidebar-right", "header-bar",
            "jake", "academic", "swiss", "harvard"
    );

    @Test
    void allThemes_haveRequiredSelectors(@TempDir Path tempDir) throws IOException {
        // Find themes directory relative to project
        Path themesDir = findThemesDir();
        for (String id : ALL_THEME_IDS) {
            Path cssFile = themesDir.resolve(id).resolve("style.css");
            assertTrue(Files.exists(cssFile), "Theme file not found: " + cssFile);
            String css = Files.readString(cssFile, StandardCharsets.UTF_8);

            String missing = "";
            for (String sel : REQUIRED_SELECTORS) {
                if (!css.contains(sel)) missing += sel + " ";
            }
            assertTrue(missing.isEmpty(),
                    id + " theme is missing selectors: " + missing);
        }
    }

    private Path findThemesDir() {
        // Try common project layouts
        List<String> candidates = List.of(
                "src/main/resources/themes",
                "../src/main/resources/themes",
                "backend/src/main/resources/themes"
        );
        Path base = Path.of(".").toAbsolutePath().normalize();
        for (String c : candidates) {
            Path p = base.resolve(c);
            if (Files.isDirectory(p)) return p;
        }
        fail("Cannot find themes directory from: " + base);
        return null;
    }
}
