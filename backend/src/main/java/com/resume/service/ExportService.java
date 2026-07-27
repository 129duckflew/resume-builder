package com.resume.service;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import com.resume.entity.Resume;
import com.resume.entity.ResumeStyle;
import com.resume.entity.Theme;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ExportService {

    private final MarkdownService markdownService;
    private final ThemeService themeService;
    private final DesensitizeService desensitizeService;
    private final ResumeStyleService resumeStyleService;
    private final LayoutSplitter layoutSplitter;
    private final ObjectMapper objectMapper = new tools.jackson.databind.json.JsonMapper();

    private volatile String baseCssCache;

    public ExportService(MarkdownService markdownService, ThemeService themeService,
                         DesensitizeService desensitizeService,
                         ResumeStyleService resumeStyleService,
                         LayoutSplitter layoutSplitter) {
        this.markdownService = markdownService;
        this.themeService = themeService;
        this.desensitizeService = desensitizeService;
        this.resumeStyleService = resumeStyleService;
        this.layoutSplitter = layoutSplitter;
    }

    public String generateHtml(Resume resume) {
        return generateHtml(resume, false, null);
    }

    public String generateHtml(Resume resume, boolean desensitize, Long userId) {
        String content = resume.getContent();
        if (desensitize) {
            content = desensitizeService.apply(content, userId);
        }
        Theme theme = themeService.findById(resume.getThemeId())
                .orElse(themeService.findById("classic").orElse(null));
        String layout = theme != null && theme.getLayout() != null ? theme.getLayout() : "single";

        String rootVars = buildRootVariablesBlock(resume);
        String themeDefaults = buildThemeDefaultsBlock(theme);

        String css;
        if (theme != null && theme.getCssContent() != null && !theme.getCssContent().isBlank()) {
            css = theme.getCssContent();
        } else {
            css = getBaseCss();
        }

        StringBuilder cssBlock = new StringBuilder();
        if (rootVars != null) cssBlock.append(rootVars).append("\n");
        if (themeDefaults != null) cssBlock.append(themeDefaults).append("\n");
        cssBlock.append(css);

        String bodyHtml = buildLayoutHtml(content, layout);

        return """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>%s</title>
<style>
%s
</style>
</head>
<body>
%s
</body>
</html>
""".formatted(escapeHtml(resume.getTitle()), cssBlock.toString(), bodyHtml);
    }

    /**
     * Build the HTML body according to the layout type.
     * {@code single} layout follows the old path — renders the entire markdown in one div.
     */
    private String buildLayoutHtml(String markdown, String layout) {
        if ("header-bar".equals(layout)) {
            return buildHeaderBarHtml(markdown);
        }

        Map<String, String> parts = layoutSplitter.split(markdown, layout);

        boolean isSidebarLayout = "sidebar-left".equals(layout) || "sidebar-right".equals(layout);

        // Sidebar layout but no sidebar sections → render main content only, wrapped in resume-main
        if (isSidebarLayout && !parts.containsKey("sidebar")) {
            String bodyHtml = markdownService.toHtml(parts.get("body"));
            return "<div class=\"resume-page\">\n<main class=\"resume-main\">\n" + bodyHtml + "\n</main>\n</div>";
        }

        // single / null → old path: whole body in one resume-page div
        if (!parts.containsKey("sidebar")) {
            String bodyHtml = markdownService.toHtml(parts.get("body"));
            return "<div class=\"resume-page\">\n" + bodyHtml + "\n</div>";
        }

        String sidebarHtml = markdownService.toHtml(parts.get("sidebar"));
        String mainHtml = markdownService.toHtml(parts.get("main"));

        if ("sidebar-right".equals(layout)) {
            return """
<div class="resume-page">
<main class="resume-main">
%s
</main>
<aside class="resume-sidebar">
%s
</aside>
</div>""".formatted(mainHtml, sidebarHtml);
        }

        // sidebar-left (default sidebar)
        return """
<div class="resume-page">
<aside class="resume-sidebar">
%s
</aside>
<main class="resume-main">
%s
</main>
</div>""".formatted(sidebarHtml, mainHtml);
    }

    /**
     * Build HTML for header-bar layout: first &lt;h2&gt; section is the header bar,
     * everything after is the body.
     */
    private String buildHeaderBarHtml(String markdown) {
        String fullHtml = markdownService.toHtml(markdown);
        int h2Index = fullHtml.indexOf("<h2");
        String headerHtml;
        String bodyHtml;
        if (h2Index >= 0) {
            headerHtml = fullHtml.substring(0, h2Index);
            bodyHtml = fullHtml.substring(h2Index);
        } else {
            headerHtml = fullHtml;
            bodyHtml = "";
        }
        return """
<div class="resume-page">
<header class="resume-header-bar">
%s
</header>
<div class="resume-body">
%s
</div>
</div>""".formatted(headerHtml, bodyHtml);
    }

    private String buildRootVariablesBlock(Resume resume) {
        // 1. Parse customVariables from ResumeStyle
        Map<String, String> vars = new HashMap<>();
        Optional<ResumeStyle> styleOpt = resumeStyleService.getStyle(resume.getId(), resume.getThemeId());
        if (styleOpt.isPresent() && styleOpt.get().getCustomVariables() != null) {
            String json = styleOpt.get().getCustomVariables();
            if (!json.equals("{}")) {
                try {
                    Map<String, String> parsed = objectMapper.readValue(json,
                            new TypeReference<Map<String, String>>() {});
                    if (parsed != null) vars.putAll(parsed);
                } catch (Exception e) {
                    // ignore parse errors
                }
            }
        }

        // 2. Old field compatibility: add if not already present in customVariables
        // Only emit if explicitly set (non-null for fontSize/lineHeight, non-default for sectionSpacing)
        if (resume.getFontSize() != null && !vars.containsKey("--font-size")) {
            vars.put("--font-size", resume.getFontSize() + "pt");
        }
        if (resume.getLineHeight() != null && !vars.containsKey("--line-height")) {
            vars.put("--line-height", String.valueOf(resume.getLineHeight()));
        }
        if (resume.getSectionSpacing() != null && !"normal".equals(resume.getSectionSpacing())
                && !vars.containsKey("--section-spacing")) {
            vars.put("--section-spacing", resume.getSectionSpacing());
        }

        if (vars.isEmpty()) return null;

        StringBuilder sb = new StringBuilder(":root {\n");
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            if (!isValidCssVarName(entry.getKey())) continue;
            sb.append("    ").append(entry.getKey()).append(": ")
                    .append(sanitizeCssValue(entry.getValue())).append(";\n");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Sanitize a CSS value to prevent injection/breakout from the :root block.
     * Escapes: backslash, curly braces, semicolons.
     */
    private String sanitizeCssValue(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(";", "\\;");
    }

    /**
     * Validate that a key is a well-formed CSS custom property name.
     * Must start with "--" and contain only letters, digits, hyphens, underscores.
     */
    private boolean isValidCssVarName(String key) {
        return key != null && key.matches("--[a-zA-Z0-9_-]+");
    }

    /**
     * Build a :root {} block from the theme's variables_schema defaults.
     * These provide the base values for CSS custom properties before user overrides.
     */
    private String buildThemeDefaultsBlock(Theme theme) {
        if (theme == null || theme.getVariablesSchema() == null) return null;
        try {
            List<Map<String, Object>> vars = objectMapper.readValue(
                    theme.getVariablesSchema(),
                    new TypeReference<List<Map<String, Object>>>() {});
            if (vars == null || vars.isEmpty()) return null;
            StringBuilder sb = new StringBuilder(":root {\n");
            for (Map<String, Object> v : vars) {
                String name = (String) v.get("name");
                String defaultValue = (String) v.get("default");
                if (name != null && defaultValue != null && isValidCssVarName(name)) {
                    sb.append("    ").append(name).append(": ")
                            .append(sanitizeCssValue(defaultValue)).append(";\n");
                }
            }
            sb.append("}");
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private String getBaseCss() {
        if (baseCssCache != null) return baseCssCache;
        synchronized (this) {
            if (baseCssCache != null) return baseCssCache;
            try (var in = getClass().getClassLoader().getResourceAsStream("templates/base.css")) {
                if (in == null) return "";
                baseCssCache = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                return baseCssCache;
            } catch (IOException e) {
                return "";
            }
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
