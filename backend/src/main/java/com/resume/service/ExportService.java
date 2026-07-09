package com.resume.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.entity.Resume;
import com.resume.entity.ResumeStyle;
import com.resume.entity.Theme;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ExportService {

    private final MarkdownService markdownService;
    private final ThemeService themeService;
    private final DesensitizeService desensitizeService;
    private final ResumeStyleService resumeStyleService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExportService(MarkdownService markdownService, ThemeService themeService,
                         DesensitizeService desensitizeService,
                         ResumeStyleService resumeStyleService) {
        this.markdownService = markdownService;
        this.themeService = themeService;
        this.desensitizeService = desensitizeService;
        this.resumeStyleService = resumeStyleService;
    }

    public String generateHtml(Resume resume) {
        return generateHtml(resume, false, null);
    }

    public String generateHtml(Resume resume, boolean desensitize, Long userId) {
        String content = resume.getContent();
        if (desensitize) {
            content = desensitizeService.apply(content, userId);
        }
        String bodyHtml = markdownService.toHtml(content);
        Theme theme = themeService.findById(resume.getThemeId())
                .orElse(themeService.findById("classic").orElse(null));
        String css = theme != null ? theme.getCssContent() : "";

        // Build :root CSS variables block
        String rootVars = buildRootVariablesBlock(resume);

        String cssBlock = rootVars != null ? rootVars + "\n" + css : css;

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
<div class="resume-page">
%s
</div>
</body>
</html>
""".formatted(escapeHtml(resume.getTitle()), cssBlock, bodyHtml);
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

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
