package com.resume.service;

import com.resume.entity.Resume;
import com.resume.entity.Theme;
import org.springframework.stereotype.Service;

import java.io.StringWriter;

@Service
public class ExportService {

    private final MarkdownService markdownService;
    private final ThemeService themeService;

    public ExportService(MarkdownService markdownService, ThemeService themeService) {
        this.markdownService = markdownService;
        this.themeService = themeService;
    }

    public String generateHtml(Resume resume) {
        String bodyHtml = markdownService.toHtml(resume.getContent());
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
