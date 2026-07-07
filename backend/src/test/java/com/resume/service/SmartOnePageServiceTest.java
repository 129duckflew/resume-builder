package com.resume.service;

import com.resume.entity.Resume;
import com.resume.service.SmartOnePageService.AdjustmentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SmartOnePageServiceTest {

    private SmartOnePageService service;

    @BeforeEach
    void setUp() {
        service = new SmartOnePageService();
    }

    @Test
    void calculateOptimalSettings_withShortContent_returnsDefaultSettings() {
        Resume resume = new Resume();
        resume.setContent("short content");
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
        String longContent = "# A\n".repeat(70);
        resume.setContent(longContent);
        resume.setThemeId("classic");

        AdjustmentResult result = service.calculateOptimalSettings(
                resume, "<p>" + "x".repeat(80 * 70) + "</p>");

        assertTrue(result.fontSize < 11f);
        assertTrue(result.lineHeight < 1.4f);
    }

    @Test
    void calculateOptimalSettings_withVeryLongContent_setsFitsOnOnePageFalse() {
        Resume resume = new Resume();
        String veryLongContent = "# A\n".repeat(100);
        resume.setContent(veryLongContent);
        resume.setThemeId("classic");

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
        resume.setThemeId("classic");
        resume.setFontSize(10f);
        resume.setLineHeight(1.3f);

        AdjustmentResult result = service.calculateOptimalSettings(resume, "<p>content</p>");

        assertEquals(10f, result.fontSize);
        assertEquals(1.3f, result.lineHeight);
    }

    @Test
    void calculateOptimalSettings_fontSizeDoesNotGoBelowMinimum() {
        Resume resume = new Resume();
        String extremelyLong = "# X\n".repeat(200);
        resume.setContent(extremelyLong);
        resume.setThemeId("classic");

        AdjustmentResult result = service.calculateOptimalSettings(resume,
                "<p>" + "x".repeat(80 * 200) + "</p>");

        assertTrue(result.fontSize >= 8f);
    }

    @Test
    void calculateOptimalSettings_lineHeightDoesNotGoBelowMinimum() {
        Resume resume = new Resume();
        String extremelyLong = "# X\n".repeat(200);
        resume.setContent(extremelyLong);
        resume.setThemeId("classic");

        AdjustmentResult result = service.calculateOptimalSettings(resume,
                "<p>" + "x".repeat(80 * 200) + "</p>");

        assertTrue(result.lineHeight >= 1.2f);
    }
}
