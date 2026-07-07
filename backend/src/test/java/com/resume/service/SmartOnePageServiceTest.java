package com.resume.service;

import com.resume.entity.Resume;
import com.resume.service.SmartOnePageService.AdjustmentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SmartOnePageServiceTest {

    private SmartOnePageService service;

    @BeforeEach
    void setUp() {
        // empty browser → falls back to estimation
        service = new SmartOnePageService(Optional.empty());
    }

    @Test
    void calculateOptimalSettings_withShortContent_returnsDefaultSettings() {
        Resume resume = new Resume();
        resume.setContent("short");
        resume.setThemeId("classic");

        AdjustmentResult result = service.calculateOptimalSettings(resume, "<p>short</p>");

        assertEquals(11f, result.fontSize, 0.01);
        assertEquals(1.4f, result.lineHeight, 0.01);
        assertTrue(result.fitsOnOnePage);
        assertNull(result.warning);
    }

    @Test
    void calculateOptimalSettings_withLongContent_reducesFontSize() {
        Resume resume = new Resume();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 70; i++) sb.append("# A\n");
        resume.setContent(sb.toString());

        AdjustmentResult result = service.calculateOptimalSettings(
                resume, "<p>" + "x".repeat(80 * 70) + "</p>");

        assertTrue(result.fontSize < 11f);
        assertTrue(result.lineHeight < 1.4f);
    }

    @Test
    void calculateOptimalSettings_withVeryLongContent_setsFitsOnOnePageFalse() {
        Resume resume = new Resume();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) sb.append("# A\n");
        resume.setContent(sb.toString());

        AdjustmentResult result = service.calculateOptimalSettings(
                resume, "<p>" + "x".repeat(80 * 100) + "</p>");

        assertFalse(result.fitsOnOnePage);
        assertNotNull(result.warning);
        assertTrue(result.warning.contains("too long"));
    }

    @Test
    void calculateOptimalSettings_usesExistingResumeSettings() {
        Resume resume = new Resume();
        resume.setContent("some content");
        resume.setFontSize(10f);
        resume.setLineHeight(1.3f);

        AdjustmentResult result = service.calculateOptimalSettings(resume, "<p>content</p>");

        assertEquals(10f, result.fontSize);
        assertEquals(1.3f, result.lineHeight);
    }

    @Test
    void injectCssVariables_injectsBeforeHeadEnd() {
        String html = "<html><head><style>body{}</style></head><body>x</body></html>";
        AdjustmentResult result = new AdjustmentResult();
        result.fontSize = 10f;
        result.lineHeight = 1.3f;
        result.sectionMargin = 12f;

        String injected = SmartOnePageService.injectCssVariables(html, result);

        assertTrue(injected.contains("--resume-font-size: 10.0pt"));
        assertTrue(injected.contains("--resume-line-height: 1.30"));
        assertTrue(injected.contains("--resume-section-margin: 12px"));
        assertTrue(injected.indexOf("--resume-font-size") < injected.indexOf("</head>"));
    }

    @Test
    void injectCssVariables_withoutHeadTag_prepends() {
        String html = "<body>x</body>";
        AdjustmentResult result = new AdjustmentResult();
        result.fontSize = 11f;

        String injected = SmartOnePageService.injectCssVariables(html, result);

        assertTrue(injected.startsWith("<style>"));
        assertTrue(injected.contains("--resume-font-size"));
    }

    @Test
    void calculateOptimalSettings_fontSizeDoesNotGoBelowMinimum() {
        Resume resume = new Resume();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) sb.append("# X\n");
        resume.setContent(sb.toString());

        AdjustmentResult result = service.calculateOptimalSettings(
                resume, "<p>" + "x".repeat(80 * 200) + "</p>");

        assertTrue(result.fontSize >= 8f);
    }

    @Test
    void calculateOptimalSettings_lineHeightDoesNotGoBelowMinimum() {
        Resume resume = new Resume();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) sb.append("# X\n");
        resume.setContent(sb.toString());

        AdjustmentResult result = service.calculateOptimalSettings(
                resume, "<p>" + "x".repeat(80 * 200) + "</p>");

        assertTrue(result.lineHeight >= 1.2f);
    }
}
