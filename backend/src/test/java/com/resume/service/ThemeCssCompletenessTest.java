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
            "h1", "h2", "h3", "p", "ul", "li", "strong", "em", "a",
            "@page", ".resume-page", "@media print"
    };

    private static final String[] REQUIRED_VARIABLES = {
            "--primary-color", "--text-color", "--heading-color", "--background-color",
            "--font-family", "--font-size",
            "--line-height", "--section-spacing"
    };

    private static final List<String> ALL_THEME_IDS = List.of(
            "classic", "modern", "minimal", "sidebar", "stackoverflow", "elegant", "compact",
            "sidebar-right", "header-bar",
            "jake", "academic", "swiss", "harvard"
    );

    @Test
    void allThemes_haveVariablesSchema() throws IOException {
        Path migrationFile = findMigrationFile();
        String sql = Files.readString(migrationFile, StandardCharsets.UTF_8);

        for (String id : ALL_THEME_IDS) {
            String schema = extractVariablesSchema(sql, id);
            assertFalse(schema.isEmpty(), "Could not find variables_schema for theme: " + id);

            String missing = "";
            for (String v : REQUIRED_VARIABLES) {
                if (!schema.contains("\"" + v + "\"")) missing += v + " ";
            }
            assertTrue(missing.isEmpty(),
                    id + " theme is missing variables: " + missing);
        }
    }

    @Test
    void baseCss_hasAllRequiredSelectors() throws IOException {
        Path baseCss = Path.of("src/main/resources/templates/base.css");
        String css = Files.readString(baseCss, StandardCharsets.UTF_8);

        String missing = "";
        for (String sel : REQUIRED_SELECTORS) {
            if (!css.contains(sel)) missing += sel + " ";
        }
        assertTrue(missing.isEmpty(),
                "base.css is missing selectors: " + missing);
    }

    private String extractVariablesSchema(String sql, String themeId) {
        Pattern insert = Pattern.compile(
                "INSERT INTO themes[^;]*?'" + themeId + "'[^;]*?'\\[([^\\]]*\\])\\s*'",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = insert.matcher(sql);
        if (m.find()) {
            return m.group(1).trim();
        }
        // Fallback: try to grab the array between the last single-quoted array and the layout param
        Pattern alt = Pattern.compile(
                "'" + themeId + "'.*?'\\[\\{.*?\\}\\]'",
                Pattern.DOTALL);
        Matcher am = alt.matcher(sql);
        if (am.find()) {
            return am.group().substring(am.group().indexOf("["));
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
