package com.resume.service;

import com.resume.entity.Resume;
import com.resume.entity.ResumeStyle;
import com.resume.entity.Theme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock
    private MarkdownService markdownService;

    @Mock
    private ThemeService themeService;

    @Mock
    private DesensitizeService desensitizeService;

    @Mock
    private ResumeStyleService resumeStyleService;

    private ExportService exportService;

    @BeforeEach
    void setUp() {
        exportService = new ExportService(markdownService, themeService, desensitizeService,
                resumeStyleService);
    }

    private Resume createResume(String themeId) {
        Resume resume = new Resume();
        resume.setId("1");
        resume.setTitle("Test Resume");
        resume.setContent("# Hello");
        resume.setThemeId(themeId);
        return resume;
    }

    @Test
    void generateHtml_withCustomVariables_injectsRootBlock() {
        Resume resume = createResume("modern");

        when(markdownService.toHtml("# Hello")).thenReturn("<h1>Hello</h1>");
        Theme theme = new Theme();
        theme.setCssContent("body { color: var(--primary-color, #000); }");
        when(themeService.findById("modern")).thenReturn(Optional.of(theme));

        ResumeStyle style = new ResumeStyle();
        style.setCustomVariables("{\"--primary-color\":\"#ff0000\",\"--font-size\":\"12pt\"}");
        when(resumeStyleService.getStyle("1", "modern")).thenReturn(Optional.of(style));

        String html = exportService.generateHtml(resume, false, 1L);

        assertTrue(html.contains(":root {"));
        assertTrue(html.contains("--primary-color: #ff0000"));
        assertTrue(html.contains("--font-size: 12pt"));
        assertTrue(html.contains("body { color: var(--primary-color, #000); }"));
    }

    @Test
    void generateHtml_withoutCustomVariables_noRootBlock() {
        Resume resume = createResume("modern");

        when(markdownService.toHtml("# Hello")).thenReturn("<h1>Hello</h1>");
        Theme theme = new Theme();
        theme.setCssContent("body { color: #000; }");
        when(themeService.findById("modern")).thenReturn(Optional.of(theme));

        when(resumeStyleService.getStyle("1", "modern")).thenReturn(Optional.empty());

        String html = exportService.generateHtml(resume, false, 1L);

        assertFalse(html.contains(":root {"));
        assertTrue(html.contains("body { color: #000; }"));
    }

    @Test
    void generateHtml_emptyCustomVariables_noRootBlock() {
        Resume resume = createResume("modern");

        when(markdownService.toHtml("# Hello")).thenReturn("<h1>Hello</h1>");
        Theme theme = new Theme();
        theme.setCssContent("body { color: #000; }");
        when(themeService.findById("modern")).thenReturn(Optional.of(theme));

        ResumeStyle style = new ResumeStyle();
        style.setCustomVariables("{}");
        when(resumeStyleService.getStyle("1", "modern")).thenReturn(Optional.of(style));

        String html = exportService.generateHtml(resume, false, 1L);

        assertFalse(html.contains(":root {"));
    }

    @Test
    void generateHtml_withFontSizeFallback_injectsFontSize() {
        Resume resume = createResume("modern");
        resume.setFontSize(12f);
        resume.setLineHeight(1.5f);
        resume.setSectionSpacing("compact");

        when(markdownService.toHtml("# Hello")).thenReturn("<h1>Hello</h1>");
        Theme theme = new Theme();
        theme.setCssContent("body { font-size: var(--font-size, 10pt); }");
        when(themeService.findById("modern")).thenReturn(Optional.of(theme));

        ResumeStyle style = new ResumeStyle();
        style.setCustomVariables("{\"--primary-color\":\"#ff0000\"}");
        when(resumeStyleService.getStyle("1", "modern")).thenReturn(Optional.of(style));

        String html = exportService.generateHtml(resume, false, 1L);

        assertTrue(html.contains(":root {"));
        assertTrue(html.contains("--primary-color: #ff0000"));
        assertTrue(html.contains("--font-size: 12.0pt"));
        assertTrue(html.contains("--line-height: 1.5"));
        assertTrue(html.contains("--section-spacing: compact"));
    }

    @Test
    void generateHtml_customVariablesOverrideOldFields() {
        Resume resume = createResume("modern");
        // --font-size already in customVariables, should NOT add old fontSize fallback
        resume.setFontSize(12f);

        when(markdownService.toHtml("# Hello")).thenReturn("<h1>Hello</h1>");
        Theme theme = new Theme();
        theme.setCssContent("body { font-size: var(--font-size, 10pt); }");
        when(themeService.findById("modern")).thenReturn(Optional.of(theme));

        ResumeStyle style = new ResumeStyle();
        style.setCustomVariables("{\"--font-size\":\"14pt\"}");
        when(resumeStyleService.getStyle("1", "modern")).thenReturn(Optional.of(style));

        String html = exportService.generateHtml(resume, false, 1L);

        assertTrue(html.contains(":root {"));
        assertTrue(html.contains("--font-size: 14pt"));
    }

    @Test
    void generateHtml_noStyleAndNoFontSize_noRootBlock() {
        Resume resume = createResume("modern");

        when(markdownService.toHtml("# Hello")).thenReturn("<h1>Hello</h1>");
        Theme theme = new Theme();
        theme.setCssContent("body { color: #000; }");
        when(themeService.findById("modern")).thenReturn(Optional.of(theme));

        when(resumeStyleService.getStyle("1", "modern")).thenReturn(Optional.empty());

        String html = exportService.generateHtml(resume, false, 1L);

        assertFalse(html.contains(":root {"));
    }

    @Test
    void generateHtml_withResumeOnlyFontSize_stillInjects() {
        Resume resume = createResume("modern");
        resume.setFontSize(11f);

        when(markdownService.toHtml("# Hello")).thenReturn("<h1>Hello</h1>");
        Theme theme = new Theme();
        theme.setCssContent("body { font-size: var(--font-size, 10pt); }");
        when(themeService.findById("modern")).thenReturn(Optional.of(theme));

        when(resumeStyleService.getStyle("1", "modern")).thenReturn(Optional.empty());

        String html = exportService.generateHtml(resume, false, 1L);

        assertTrue(html.contains(":root {"));
        assertTrue(html.contains("--font-size: 11.0pt"));
    }

    // ---- P1: sectionSpacing="normal" boundary ----

    @Test
    void generateHtml_sectionSpacingNormal_noRootBlock() {
        Resume resume = createResume("modern");
        resume.setSectionSpacing("normal");

        when(markdownService.toHtml("# Hello")).thenReturn("<h1>Hello</h1>");
        Theme theme = new Theme();
        theme.setCssContent("body { color: #000; }");
        when(themeService.findById("modern")).thenReturn(Optional.of(theme));
        when(resumeStyleService.getStyle("1", "modern")).thenReturn(Optional.empty());

        String html = exportService.generateHtml(resume, false, 1L);

        assertFalse(html.contains(":root {"),
                "sectionSpacing=normal alone should not inject any :root block");
    }

    @Test
    void generateHtml_sectionSpacingNormalWithCustomVariables_doesNotInjectSectionSpacing() {
        Resume resume = createResume("modern");
        resume.setSectionSpacing("normal");

        when(markdownService.toHtml("# Hello")).thenReturn("<h1>Hello</h1>");
        Theme theme = new Theme();
        theme.setCssContent("body { color: var(--primary-color, #000); }");
        when(themeService.findById("modern")).thenReturn(Optional.of(theme));

        ResumeStyle style = new ResumeStyle();
        style.setCustomVariables("{\"--primary-color\":\"#ff0000\"}");
        when(resumeStyleService.getStyle("1", "modern")).thenReturn(Optional.of(style));

        String html = exportService.generateHtml(resume, false, 1L);

        assertTrue(html.contains(":root {"), "should have :root block from customVariables");
        assertTrue(html.contains("--primary-color: #ff0000"));
        assertFalse(html.contains("--section-spacing"),
                "sectionSpacing=normal should NOT inject --section-spacing");
    }

    // ---- P0: CSS injection protection ----

    @Test
    void generateHtml_maliciousCustomVariableValue_escaped() {
        Resume resume = createResume("modern");

        when(markdownService.toHtml("# Hello")).thenReturn("<h1>Hello</h1>");
        Theme theme = new Theme();
        theme.setCssContent("body { color: #000; }");
        when(themeService.findById("modern")).thenReturn(Optional.of(theme));

        // Value that attempts to break out of the :root block
        ResumeStyle style = new ResumeStyle();
        style.setCustomVariables(
                "{\"--color\":\"#ff0000; } body { background-image: url('http://evil.com'); } :root { --x:\"}");
        when(resumeStyleService.getStyle("1", "modern")).thenReturn(Optional.of(style));

        String html = exportService.generateHtml(resume, false, 1L);

        // The malicious content should be escaped and NOT break the :root block
        assertTrue(html.contains(":root {"), "should have a :root block");
        // Escaped semicolons and braces show sanitization worked
        assertTrue(html.contains("\\;"), "semicolons in value should be escaped");
        assertTrue(html.contains("\\}"), "closing braces in value should be escaped");
        // There should be exactly one :root{ declaration in the output
        assertEquals(1, countOccurrences(html, ":root {"),
                "should have exactly one :root block");
    }

    /** Count occurrences of a substring in a string. */
    private int countOccurrences(String str, String sub) {
        int count = 0, idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    @Test
    void generateHtml_maliciousCustomVariableKey_skipped() {
        Resume resume = createResume("modern");

        when(markdownService.toHtml("# Hello")).thenReturn("<h1>Hello</h1>");
        Theme theme = new Theme();
        theme.setCssContent("body { color: #000; }");
        when(themeService.findById("modern")).thenReturn(Optional.of(theme));

        // Key with illegal characters attempting CSS injection
        ResumeStyle style = new ResumeStyle();
        style.setCustomVariables(
                "{\"--valid-key\":\"#00ff00\",\"--x; } body { color: red; } :root { --y\":\"ignored\"}");
        when(resumeStyleService.getStyle("1", "modern")).thenReturn(Optional.of(style));

        String html = exportService.generateHtml(resume, false, 1L);

        assertTrue(html.contains(":root {"));
        assertTrue(html.contains("--valid-key: #00ff00"), "valid key should be present");
        assertFalse(html.contains("color: red"),
                "malicious key should be skipped and not inject CSS");
    }
}
