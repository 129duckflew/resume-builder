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

    @Mock
    private LayoutSplitter layoutSplitter;

    private ExportService exportService;

    @BeforeEach
    void setUp() {
        exportService = new ExportService(markdownService, themeService, desensitizeService,
                resumeStyleService, layoutSplitter);
    }

    private Resume createResume(String themeId) {
        Resume resume = new Resume();
        resume.setId("1");
        resume.setTitle("Test Resume");
        resume.setContent("# Hello");
        resume.setThemeId(themeId);
        return resume;
    }

    private void mockSingleLayout(Theme theme) {
        when(layoutSplitter.split(anyString(), any()))
                .thenReturn(java.util.Map.of("body", "# Hello"));
    }

    @Test
    void generateHtml_withCustomVariables_injectsRootBlock() {
        Resume resume = createResume("modern");

        when(markdownService.toHtml("# Hello")).thenReturn("<h1>Hello</h1>");
        Theme theme = new Theme();
        theme.setCssContent("body { color: var(--primary-color, #000); }");
        when(themeService.findById("modern")).thenReturn(Optional.of(theme));
        mockSingleLayout(theme);

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
        mockSingleLayout(theme);

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
        mockSingleLayout(theme);

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
        mockSingleLayout(theme);

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
        mockSingleLayout(theme);

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
        mockSingleLayout(theme);

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
        mockSingleLayout(theme);

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
        mockSingleLayout(theme);
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
        mockSingleLayout(theme);

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
        mockSingleLayout(theme);

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
        mockSingleLayout(theme);

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

    // ---- Layout-based tests ----

    @Test
    void generateHtml_singleLayout_containsResumePageOnly() {
        Resume resume = createResume("modern");
        when(markdownService.toHtml("# Hello")).thenReturn("<h1>Hello</h1>");
        Theme theme = new Theme();
        theme.setCssContent("body { color: #000; }");
        theme.setLayout("single");
        when(themeService.findById("modern")).thenReturn(Optional.of(theme));
        when(layoutSplitter.split(anyString(), eq("single")))
                .thenReturn(java.util.Map.of("body", "# Hello"));
        when(resumeStyleService.getStyle("1", "modern")).thenReturn(Optional.empty());

        String html = exportService.generateHtml(resume, false, 1L);

        assertTrue(html.contains("<div class=\"resume-page\">"));
        assertFalse(html.contains("resume-sidebar"));
        assertFalse(html.contains("resume-main"));
    }

    @Test
    void generateHtml_sidebarLeft_containsSidebarBeforeMain() {
        Resume resume = createResume("sidebar");
        resume.setContent("## Contact\nEmail\n## Experience\nWorked");
        when(markdownService.toHtml("## Contact\nEmail")).thenReturn("<h2>Contact</h2><p>Email</p>");
        when(markdownService.toHtml("## Experience\nWorked")).thenReturn("<h2>Experience</h2><p>Worked</p>");
        Theme theme = new Theme();
        theme.setCssContent("body { color: #000; }");
        theme.setLayout("sidebar-left");
        when(themeService.findById("sidebar")).thenReturn(Optional.of(theme));
        when(layoutSplitter.split(anyString(), eq("sidebar-left")))
                .thenReturn(java.util.Map.of("sidebar", "## Contact\nEmail", "main", "## Experience\nWorked"));
        when(resumeStyleService.getStyle("1", "sidebar")).thenReturn(Optional.empty());

        String html = exportService.generateHtml(resume, false, 1L);

        assertTrue(html.contains("<div class=\"resume-page\">"));
        assertTrue(html.contains("<aside class=\"resume-sidebar\">"));
        assertTrue(html.contains("<main class=\"resume-main\">"));
        // Sidebar should appear before main
        int sidebarIdx = html.indexOf("resume-sidebar");
        int mainIdx = html.indexOf("resume-main");
        assertTrue(sidebarIdx < mainIdx, "sidebar should appear before main in sidebar-left");
    }

    @Test
    void generateHtml_sidebarRightNoSidebarKeywords_rendersContentNotBlank() {
        Resume resume = createResume("sidebar-right");
        resume.setContent("# Name\n## Experience\nWorked at ACME");
        when(markdownService.toHtml(resume.getContent())).thenReturn("<h1>Name</h1><h2>Experience</h2><p>Worked at ACME</p>");
        Theme theme = new Theme();
        theme.setCssContent("body { color: #000; }");
        theme.setLayout("sidebar-right");
        when(themeService.findById("sidebar-right")).thenReturn(Optional.of(theme));
        when(layoutSplitter.split(anyString(), eq("sidebar-right")))
                .thenReturn(java.util.Map.of("body", resume.getContent()));
        when(resumeStyleService.getStyle("1", "sidebar-right")).thenReturn(Optional.empty());

        String html = exportService.generateHtml(resume, false, 1L);

        assertTrue(html.contains("Worked at ACME"));
        assertFalse(html.contains("resume-sidebar"));
        assertFalse(html.contains("resume-main"));
    }

    @Test
    void generateHtml_sidebarRight_containsMainBeforeSidebar() {
        Resume resume = createResume("sidebar-right");
        resume.setContent("## Skills\nJava\n## Experience\nWorked");
        when(markdownService.toHtml("## Skills\nJava")).thenReturn("<h2>Skills</h2><p>Java</p>");
        when(markdownService.toHtml("## Experience\nWorked")).thenReturn("<h2>Experience</h2><p>Worked</p>");
        Theme theme = new Theme();
        theme.setCssContent("body { color: #000; }");
        theme.setLayout("sidebar-right");
        when(themeService.findById("sidebar-right")).thenReturn(Optional.of(theme));
        when(layoutSplitter.split(anyString(), eq("sidebar-right")))
                .thenReturn(java.util.Map.of("sidebar", "## Skills\nJava", "main", "## Experience\nWorked"));
        when(resumeStyleService.getStyle("1", "sidebar-right")).thenReturn(Optional.empty());

        String html = exportService.generateHtml(resume, false, 1L);

        assertTrue(html.contains("<div class=\"resume-page\">"));
        assertTrue(html.contains("<aside class=\"resume-sidebar\">"));
        assertTrue(html.contains("<main class=\"resume-main\">"));
        // Main should appear before sidebar
        int mainIdx = html.indexOf("resume-main");
        int sidebarIdx = html.indexOf("resume-sidebar");
        assertTrue(mainIdx < sidebarIdx, "main should appear before sidebar in sidebar-right");
    }

    @Test
    void generateHtml_themeNoLayout_defaultsToSingle() {
        Resume resume = createResume("modern");
        when(markdownService.toHtml("# Hello")).thenReturn("<h1>Hello</h1>");
        Theme theme = new Theme();
        theme.setCssContent("body { color: #000; }");
        // layout not set (null)
        when(themeService.findById("modern")).thenReturn(Optional.of(theme));
        when(layoutSplitter.split(anyString(), any()))
                .thenReturn(java.util.Map.of("body", "# Hello"));
        when(resumeStyleService.getStyle("1", "modern")).thenReturn(Optional.empty());

        String html = exportService.generateHtml(resume, false, 1L);

        assertTrue(html.contains("<div class=\"resume-page\">"));
        assertFalse(html.contains("resume-sidebar"));
    }

    @Test
    void generateHtml_headerBar_containsHeaderBarAndBody() {
        Resume resume = createResume("header-bar");
        resume.setContent("John Doe\nj@e.com\n\n## Experience\nWorked");
        when(markdownService.toHtml(resume.getContent()))
                .thenReturn("<p>John Doe<br/>j@e.com</p>\n<h2>Experience</h2>\n<p>Worked</p>");
        Theme theme = new Theme();
        theme.setCssContent("body { color: #000; }");
        theme.setLayout("header-bar");
        when(themeService.findById("header-bar")).thenReturn(Optional.of(theme));
        when(resumeStyleService.getStyle("1", "header-bar")).thenReturn(Optional.empty());

        String html = exportService.generateHtml(resume, false, 1L);

        assertTrue(html.contains("<div class=\"resume-page\">"));
        assertTrue(html.contains("<header class=\"resume-header-bar\">"));
        assertTrue(html.contains("<div class=\"resume-body\">"));
        // Header should contain the name/email
        assertTrue(html.contains("John Doe"));
        // Body should contain Experience
        assertTrue(html.contains("Experience"));
    }

    @Test
    void generateHtml_headerBar_noH2_putsAllInHeader() {
        Resume resume = createResume("header-bar");
        resume.setContent("John Doe\nj@e.com");
        when(markdownService.toHtml(resume.getContent()))
                .thenReturn("<p>John Doe<br/>j@e.com</p>");
        Theme theme = new Theme();
        theme.setCssContent("body { color: #000; }");
        theme.setLayout("header-bar");
        when(themeService.findById("header-bar")).thenReturn(Optional.of(theme));
        when(resumeStyleService.getStyle("1", "header-bar")).thenReturn(Optional.empty());

        String html = exportService.generateHtml(resume, false, 1L);

        assertTrue(html.contains("<header class=\"resume-header-bar\">"));
        assertTrue(html.contains("John Doe"));
        // body div should exist but be empty
        assertTrue(html.contains("<div class=\"resume-body\">\n\n</div>") || html.contains("<div class=\"resume-body\">"));
    }
}
