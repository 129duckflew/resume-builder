package com.resume.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void allThemes_haveRequiredSelectors() throws IOException {
        Path migrationFile = findMigrationFile();
        String sql = Files.readString(migrationFile, StandardCharsets.UTF_8);

        for (String id : ALL_THEME_IDS) {
            String css = extractCssForTheme(sql, id);
            assertFalse(css.isEmpty(), "Could not find CSS for theme: " + id);

            String missing = "";
            for (String sel : REQUIRED_SELECTORS) {
                if (!css.contains(sel)) missing += sel + " ";
            }
            assertTrue(missing.isEmpty(),
                    id + " theme is missing selectors: " + missing);
        }
    }

    private String extractCssForTheme(String sql, String themeId) {
        Pattern insert = Pattern.compile(
                "INSERT INTO themes[^;]*?'" + themeId + "'[^;]*?\\$\\$(.*?)\\$\\$",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = insert.matcher(sql);
        if (m.find()) {
            return m.group(1).trim();
        }
        return "";
    }

    private Path findMigrationFile() {
        List<String> candidates = List.of(
                "src/main/resources/db/migration/V2__seed_themes.sql",
                "../src/main/resources/db/migration/V2__seed_themes.sql",
                "backend/src/main/resources/db/migration/V2__seed_themes.sql"
        );
        Path base = Path.of(".").toAbsolutePath().normalize();
        for (String c : candidates) {
            Path p = base.resolve(c);
            if (Files.isRegularFile(p)) return p;
        }
        fail("Cannot find V2 seed migration from: " + base);
        return null;
    }
}
