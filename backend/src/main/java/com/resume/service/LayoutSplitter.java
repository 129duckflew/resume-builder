package com.resume.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LayoutSplitter {

    private static final Set<String> SIDEBAR_KEYWORDS = Set.of(
            "contact", "contacts", "skills", "skill", "languages", "language",
            "summary", "profile", "personal", "objective",
            "certifications", "certification", "interests", "hobbies", "references"
    );

    /**
     * Split markdown content according to the given layout type.
     * <ul>
     *   <li>{@code single} / {@code header-bar} / {@code null} → returns {@code {"body": markdown}}</li>
     *   <li>{@code sidebar-left} / {@code sidebar-right} → splits by H2 sections, classifies
     *       section titles into sidebar (contact/skills/etc.) vs main (everything else)</li>
     * </ul>
     */
    public Map<String, String> split(String markdown, String layout) {
        if (layout == null || layout.equals("single") || layout.equals("header-bar")) {
            return Collections.singletonMap("body", markdown == null ? "" : markdown);
        }

        if (markdown == null || markdown.isBlank()) {
            return Collections.singletonMap("body", markdown == null ? "" : markdown);
        }

        // sidebar-left / sidebar-right: split into sections on H2 headings
        // Regex: newline followed by "## " (H2 heading)
        String[] sections = markdown.split("\\n(?=## )");

        StringBuilder sidebar = new StringBuilder();
        StringBuilder main = new StringBuilder();

        for (String section : sections) {
            String trimmed = section.trim();
            String title = extractH2Title(trimmed);
            if (title != null && isSidebarKeyword(title)) {
                if (sidebar.length() > 0) sidebar.append("\n");
                sidebar.append(trimmed);
            } else {
                if (main.length() > 0) main.append("\n");
                main.append(trimmed);
            }
        }

        Map<String, String> result = new LinkedHashMap<>();
        // Only include sidebar key if there's actually sidebar content
        if (sidebar.length() > 0) {
            result.put("sidebar", sidebar.toString().trim());
        }
        result.put("main", main.toString().trim());
        return result;
    }

    /** Extract the H2 title from a section (line starting with "## ") */
    private String extractH2Title(String section) {
        for (String line : section.split("\\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("## ")) {
                return trimmed.substring(3).trim().toLowerCase();
            }
        }
        return null;
    }

    private boolean isSidebarKeyword(String title) {
        return SIDEBAR_KEYWORDS.contains(title);
    }
}
