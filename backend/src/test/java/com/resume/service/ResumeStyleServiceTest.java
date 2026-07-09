package com.resume.service;

import com.resume.dto.ResumeStyleDTO;
import com.resume.entity.ResumeStyle;
import com.resume.repository.ResumeStyleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeStyleServiceTest {

    @Mock
    private ResumeStyleRepository repository;

    private ResumeStyleService service;

    @BeforeEach
    void setUp() {
        service = new ResumeStyleService(repository);
    }

    @Test
    void getStyle_existing_returnsStyle() {
        ResumeStyle style = new ResumeStyle();
        style.setResumeId("r1");
        style.setThemeId("classic");
        style.setFontSize(12f);

        when(repository.findByResumeIdAndThemeId("r1", "classic")).thenReturn(Optional.of(style));

        Optional<ResumeStyle> result = service.getStyle("r1", "classic");
        assertTrue(result.isPresent());
        assertEquals(12f, result.get().getFontSize());
    }

    @Test
    void getStyle_missing_returnsEmpty() {
        when(repository.findByResumeIdAndThemeId("r1", "missing")).thenReturn(Optional.empty());
        assertTrue(service.getStyle("r1", "missing").isEmpty());
    }

    @Test
    void saveStyle_new_creates() {
        ResumeStyleDTO dto = new ResumeStyleDTO();
        dto.setFontSize(14f);
        dto.setLineHeight(1.5f);
        dto.setSectionSpacing("compact");
        dto.setCustomVariables(Map.of("--primary-color", "#ff0000"));

        when(repository.findByResumeIdAndThemeId("r1", "modern")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        ResumeStyle result = service.saveStyle("r1", "modern", dto);

        assertEquals("r1", result.getResumeId());
        assertEquals("modern", result.getThemeId());
        assertEquals(14f, result.getFontSize());
        assertEquals(1.5f, result.getLineHeight());
        assertEquals("compact", result.getSectionSpacing());
        assertNotNull(result.getCustomVariables());
        assertTrue(result.getCustomVariables().contains("#ff0000"));
    }

    @Test
    void saveStyle_existing_updates() {
        ResumeStyle existing = new ResumeStyle();
        existing.setResumeId("r1");
        existing.setThemeId("classic");
        existing.setFontSize(10f);

        ResumeStyleDTO dto = new ResumeStyleDTO();
        dto.setFontSize(12f);
        dto.setLineHeight(1.5f);
        // fontSize should be updated, lineHeight set, sectionSpacing stays null

        when(repository.findByResumeIdAndThemeId("r1", "classic")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        ResumeStyle result = service.saveStyle("r1", "classic", dto);

        assertEquals(12f, result.getFontSize());
        assertEquals(1.5f, result.getLineHeight());
        assertNull(result.getSectionSpacing());
    }

    @Test
    void saveStyle_nullFields_dontOverride() {
        ResumeStyle existing = new ResumeStyle();
        existing.setResumeId("r1");
        existing.setThemeId("classic");
        existing.setFontSize(10f);
        existing.setLineHeight(1.2f);
        existing.setCustomVariables("{\"--old\":\"value\"}");

        ResumeStyleDTO dto = new ResumeStyleDTO();
        dto.setFontSize(null);
        dto.setLineHeight(1.5f);
        dto.setCustomVariables(null); // null should not override

        when(repository.findByResumeIdAndThemeId("r1", "classic")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        ResumeStyle result = service.saveStyle("r1", "classic", dto);

        assertEquals(10f, result.getFontSize());  // unchanged
        assertEquals(1.5f, result.getLineHeight()); // updated
        assertNotNull(result.getCustomVariables()); // unchanged
        assertTrue(result.getCustomVariables().contains("--old"));
    }

    @Test
    void saveStyle_customVariables_emptyMapClears() {
        ResumeStyle existing = new ResumeStyle();
        existing.setResumeId("r1");
        existing.setThemeId("classic");
        existing.setCustomVariables("{\"--old\":\"value\"}");

        ResumeStyleDTO dto = new ResumeStyleDTO();
        dto.setCustomVariables(Map.of()); // empty map should clear

        when(repository.findByResumeIdAndThemeId("r1", "classic")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        ResumeStyle result = service.saveStyle("r1", "classic", dto);

        assertNotNull(result.getCustomVariables());
        assertEquals("{}", result.getCustomVariables());
    }

    @Test
    void getStyle_returnsCustomVariables() {
        ResumeStyle style = new ResumeStyle();
        style.setResumeId("r1");
        style.setThemeId("classic");
        style.setCustomVariables("{\"--primary-color\":\"#ff0000\"}");

        when(repository.findByResumeIdAndThemeId("r1", "classic")).thenReturn(Optional.of(style));

        Optional<ResumeStyle> result = service.getStyle("r1", "classic");
        assertTrue(result.isPresent());
        assertNotNull(result.get().getCustomVariables());
        assertTrue(result.get().getCustomVariables().contains("--primary-color"));
    }
}
