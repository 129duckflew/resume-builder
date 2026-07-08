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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock
    private ResumeRepository repository;

    @Mock
    private ResumeVersionService versionService;

    private ResumeService service;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        service = new ResumeService(repository, versionService);
    }

    @Test
    void findByUserId_returnsResumesForUser() {
        var r1 = new Resume();
        r1.setId("1");
        when(repository.findByUserIdOrderByUpdatedAtDesc(userId)).thenReturn(List.of(r1));

        List<Resume> result = service.findByUserId(userId);

        assertEquals(1, result.size());
        verify(repository).findByUserIdOrderByUpdatedAtDesc(userId);
    }

    @Test
    void findByIdAndUserId_withExistingId_returnsResume() {
        var resume = new Resume();
        resume.setId("abc");
        when(repository.findByIdAndUserId("abc", userId)).thenReturn(Optional.of(resume));

        Optional<Resume> result = service.findByIdAndUserId("abc", userId);

        assertTrue(result.isPresent());
        assertEquals("abc", result.get().getId());
    }

    @Test
    void findByIdAndUserId_withNonExistingId_returnsEmpty() {
        when(repository.findByIdAndUserId("missing", userId)).thenReturn(Optional.empty());

        Optional<Resume> result = service.findByIdAndUserId("missing", userId);

        assertFalse(result.isPresent());
    }

    @Test
    void create_createsResumeWithUserId() {
        ResumeDTO dto = new ResumeDTO();
        dto.setTitle("My Resume");

        Resume saved = new Resume();
        saved.setId("new-id");
        saved.setUserId(userId);
        saved.setTitle("My Resume");
        when(repository.save(any())).thenReturn(saved);

        Resume result = service.create(dto, userId);

        assertEquals("new-id", result.getId());
        assertEquals(userId, result.getUserId());
        verify(repository).save(any());
    }

    @Test
    void update_updatesExistingResume() {
        ResumeDTO dto = new ResumeDTO();
        dto.setTitle("Updated");

        var existing = new Resume();
        existing.setId("1");
        existing.setTitle("Old");
        existing.setUserId(userId);

        when(repository.findByIdAndUserId("1", userId)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Resume result = service.update("1", dto, userId);

        assertEquals("Updated", result.getTitle());
    }

    @Test
    void update_withNonExistingId_throws() {
        ResumeDTO dto = new ResumeDTO();
        dto.setTitle("X");
        when(repository.findByIdAndUserId("missing", userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.update("missing", dto, userId));
    }

    @Test
    void delete_callsRepository() {
        var resume = new Resume();
        resume.setId("1");
        when(repository.findByIdAndUserId("1", userId)).thenReturn(Optional.of(resume));

        service.delete("1", userId);
        verify(repository).delete(resume);
    }

    @Test
    void update_withPartialFields_onlyUpdatesNonNullFields() {
        var existing = new Resume();
        existing.setId("1");
        existing.setTitle("Old Title");
        existing.setContent("Old Content");
        existing.setThemeId("classic");
        existing.setUserId(userId);

        ResumeDTO dto = new ResumeDTO();
        dto.setThemeId("modern");

        when(repository.findByIdAndUserId("1", userId)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Resume result = service.update("1", dto, userId);

        assertEquals("Old Title", result.getTitle());
        assertEquals("Old Content", result.getContent());
        assertEquals("modern", result.getThemeId());
    }

    @Test
    void assignOrphanResumes_assignsUserId() {
        var orphan = new Resume();
        orphan.setId("1");
        orphan.setUserId(null);

        when(repository.findByUserIdIsNull()).thenReturn(List.of(orphan));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.assignOrphanResumes(userId);

        assertEquals(userId, orphan.getUserId());
    }
}
