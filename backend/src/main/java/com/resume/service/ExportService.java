package com.resume.service;

import com.resume.entity.Resume;
import com.resume.entity.Theme;
import org.springframework.stereotype.Service;

@Service
public class ExportService {

    private final MarkdownService markdownService;
    private final ThemeService themeService;
    private final DesensitizeService desensitizeService;

    public ExportService(MarkdownService markdownService, ThemeService themeService,
                         DesensitizeService desensitizeService) {
        this.markdownService = markdownService;
        this.themeService = themeService;
        this.desensitizeService = desensitizeService;
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
""".formatted(escapeHtml(resume.getTitle()), css, bodyHtml);
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
