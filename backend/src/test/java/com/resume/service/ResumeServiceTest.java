package com.resume.service;

import com.resume.dto.ResumeDTO;
import com.resume.entity.Resume;
import com.resume.repository.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock
    private ResumeRepository repository;

    private ResumeService service;

    @BeforeEach
    void setUp() {
        service = new ResumeService(repository);
    }

    @Test
    void findAll_returnsAllResumes() {
        var r1 = new Resume();
        r1.setId("1");
        var r2 = new Resume();
        r2.setId("2");
        when(repository.findAllByOrderByUpdatedAtDesc()).thenReturn(List.of(r1, r2));

        List<Resume> result = service.findAll();

        assertEquals(2, result.size());
        verify(repository).findAllByOrderByUpdatedAtDesc();
    }

    @Test
    void findById_withExistingId_returnsResume() {
        var resume = new Resume();
        resume.setId("abc");
        when(repository.findById("abc")).thenReturn(Optional.of(resume));

        Optional<Resume> result = service.findById("abc");

        assertTrue(result.isPresent());
        assertEquals("abc", result.get().getId());
    }

    @Test
    void findById_withNonExistingId_returnsEmpty() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        Optional<Resume> result = service.findById("missing");

        assertFalse(result.isPresent());
    }

    @Test
    void create_createsResumeWithDefaults() {
        ResumeDTO dto = new ResumeDTO();
        dto.setTitle("My Resume");

        Resume saved = new Resume();
        saved.setId("new-id");
        saved.setTitle("My Resume");
        when(repository.save(any())).thenReturn(saved);

        Resume result = service.create(dto);

        assertEquals("new-id", result.getId());
        assertEquals("My Resume", result.getTitle());
        verify(repository).save(any());
    }

    @Test
    void create_usesProvidedContent() {
        ResumeDTO dto = new ResumeDTO();
        dto.setTitle("Test");
        dto.setContent("# Custom Content");
        dto.setThemeId("modern");

        Resume saved = new Resume();
        saved.setId("1");
        saved.setTitle("Test");
        saved.setContent("# Custom Content");
        saved.setThemeId("modern");
        when(repository.save(any())).thenReturn(saved);

        Resume result = service.create(dto);

        assertEquals("# Custom Content", result.getContent());
        assertEquals("modern", result.getThemeId());
    }

    @Test
    void update_updatesExistingResume() {
        ResumeDTO dto = new ResumeDTO();
        dto.setTitle("Updated");
        dto.setContent("# Updated");
        dto.setThemeId("minimal");

        var existing = new Resume();
        existing.setId("1");
        existing.setTitle("Old");
        existing.setContent("old");
        existing.setThemeId("classic");

        when(repository.findById("1")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Resume result = service.update("1", dto);

        assertEquals("Updated", result.getTitle());
        assertEquals("# Updated", result.getContent());
        assertEquals("minimal", result.getThemeId());
    }

    @Test
    void update_withNonExistingId_throws() {
        ResumeDTO dto = new ResumeDTO();
        dto.setTitle("X");
        dto.setContent("x");

        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.update("missing", dto));
    }

    @Test
    void update_withPartialFields_onlyUpdatesNonNullFields() {
        var existing = new Resume();
        existing.setId("1");
        existing.setTitle("Old Title");
        existing.setContent("Old Content");
        existing.setThemeId("classic");

        ResumeDTO dto = new ResumeDTO();
        dto.setThemeId("modern");
        // title and content are intentionally null

        when(repository.findById("1")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Resume result = service.update("1", dto);

        // Fields not sent should retain original values
        assertEquals("Old Title", result.getTitle());
        assertEquals("Old Content", result.getContent());
        // Only themeId should be updated
        assertEquals("modern", result.getThemeId());
    }

    @Test
    void delete_callsRepository() {
        service.delete("1");
        verify(repository).deleteById("1");
    }
}
