package com.resume.service;

import com.resume.entity.ResumeStyle;
import com.resume.repository.ResumeStyleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        ResumeStyle incoming = new ResumeStyle();
        incoming.setFontSize(14f);
        incoming.setLineHeight(1.5f);
        incoming.setSectionSpacing("compact");

        when(repository.findByResumeIdAndThemeId("r1", "modern")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        ResumeStyle result = service.saveStyle("r1", "modern", incoming);

        assertEquals("r1", result.getResumeId());
        assertEquals("modern", result.getThemeId());
        assertEquals(14f, result.getFontSize());
        assertEquals(1.5f, result.getLineHeight());
        assertEquals("compact", result.getSectionSpacing());
    }

    @Test
    void saveStyle_existing_updates() {
        ResumeStyle existing = new ResumeStyle();
        existing.setResumeId("r1");
        existing.setThemeId("classic");
        existing.setFontSize(10f);

        ResumeStyle incoming = new ResumeStyle();
        incoming.setFontSize(12f);
        incoming.setLineHeight(1.5f);
        // fontSize should be updated, lineHeight set, sectionSpacing stays null

        when(repository.findByResumeIdAndThemeId("r1", "classic")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        ResumeStyle result = service.saveStyle("r1", "classic", incoming);

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

        ResumeStyle incoming = new ResumeStyle();
        incoming.setFontSize(null);
        incoming.setLineHeight(1.5f);

        when(repository.findByResumeIdAndThemeId("r1", "classic")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        ResumeStyle result = service.saveStyle("r1", "classic", incoming);

        assertEquals(10f, result.getFontSize());  // unchanged
        assertEquals(1.5f, result.getLineHeight()); // updated
    }
}
