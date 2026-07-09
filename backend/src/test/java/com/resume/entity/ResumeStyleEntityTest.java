package com.resume.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResumeStyleEntityTest {

    @Test
    void resumeStyle_hasCustomVariablesField() {
        ResumeStyle style = new ResumeStyle();
        style.setCustomVariables("{\"--primary-color\":\"#ff0000\"}");
        
        assertNotNull(style.getCustomVariables());
        assertTrue(style.getCustomVariables().contains("#ff0000"));
    }

    @Test
    void resumeStyle_customVariables_canBeNull() {
        ResumeStyle style = new ResumeStyle();
        style.setResumeId("r1");
        assertNull(style.getCustomVariables());
    }

    @Test
    void resumeStyle_oldFieldsStillExist() {
        ResumeStyle style = new ResumeStyle();
        style.setFontSize(12f);
        style.setLineHeight(1.5f);
        style.setSectionSpacing("compact");
        
        assertEquals(12f, style.getFontSize());
        assertEquals(1.5f, style.getLineHeight());
        assertEquals("compact", style.getSectionSpacing());
    }
}
