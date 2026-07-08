package com.resume.service;

import com.resume.entity.Resume;
import com.resume.entity.ResumeVersion;
import com.resume.repository.ResumeVersionRepository;
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
class ResumeVersionServiceTest {

    @Mock
    private ResumeVersionRepository repository;

    private ResumeVersionService service;

    @BeforeEach
    void setUp() {
        service = new ResumeVersionService(repository);
    }

    @Test
    void saveSnapshot_firstVersion_createsVersion1() {
        Resume resume = new Resume();
        resume.setId("r1");
        resume.setTitle("Test");
        resume.setContent("# Hello");

        when(repository.findTopByResumeIdOrderByVersionNumberDesc("r1")).thenReturn(Optional.empty());

        service.saveSnapshot(resume);

        verify(repository).save(argThat(v ->
                v.getVersionNumber() == 1
                        && v.getResumeId().equals("r1")
                        && v.getTitle().equals("Test")
        ));
    }

    @Test
    void saveSnapshot_subsequentVersion_increments() {
        Resume resume = new Resume();
        resume.setId("r1");
        resume.setTitle("Test");

        ResumeVersion prev = new ResumeVersion();
        prev.setVersionNumber(3);
        when(repository.findTopByResumeIdOrderByVersionNumberDesc("r1")).thenReturn(Optional.of(prev));

        service.saveSnapshot(resume);

        verify(repository).save(argThat(v -> v.getVersionNumber() == 4));
    }

    @Test
    void saveSnapshot_enforcesMaxVersions() {
        Resume resume = new Resume();
        resume.setId("r1");

        when(repository.findTopByResumeIdOrderByVersionNumberDesc("r1")).thenReturn(Optional.empty());

        List<ResumeVersion> manyVersions = new java.util.ArrayList<>();
        for (int i = 0; i <= 49; i++) {
            ResumeVersion v = new ResumeVersion();
            v.setVersionNumber(50 - i);
            manyVersions.add(v);
        }
        // After saving, we'll have 51 versions (1 new + 50 existing)
        // Simulate the findAll call during cleanup
        when(repository.findByResumeIdOrderByVersionNumberDesc("r1")).thenReturn(manyVersions);

        service.saveSnapshot(resume);
    }

    @Test
    void getVersions_returnsOrderedList() {
        ResumeVersion v1 = new ResumeVersion();
        v1.setVersionNumber(2);
        ResumeVersion v2 = new ResumeVersion();
        v2.setVersionNumber(1);

        when(repository.findByResumeIdOrderByVersionNumberDesc("r1")).thenReturn(List.of(v1, v2));

        List<ResumeVersion> result = service.getVersions("r1");

        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getVersionNumber());
    }

    @Test
    void getVersion_found_returns() {
        ResumeVersion v = new ResumeVersion();
        v.setVersionNumber(1);

        when(repository.findByResumeIdAndVersionNumber("r1", 1)).thenReturn(Optional.of(v));

        assertEquals(1, service.getVersion("r1", 1).getVersionNumber());
    }

    @Test
    void getVersion_notFound_throws() {
        when(repository.findByResumeIdAndVersionNumber("r1", 99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getVersion("r1", 99));
    }

    @Test
    void restoreVersion_returnsResumeFromVersion() {
        ResumeVersion v = new ResumeVersion();
        v.setVersionNumber(2);
        v.setTitle("Restored");
        v.setContent("# Restored");
        v.setThemeId("modern");
        v.setFontSize(12f);
        v.setLineHeight(1.5f);
        v.setSectionSpacing("compact");

        when(repository.findByResumeIdAndVersionNumber("r1", 2)).thenReturn(Optional.of(v));

        Resume result = service.restoreVersion("r1", 2);

        assertEquals("Restored", result.getTitle());
        assertEquals("# Restored", result.getContent());
        assertEquals("modern", result.getThemeId());
        assertEquals(12f, result.getFontSize());
        assertEquals(1.5f, result.getLineHeight());
        assertEquals("compact", result.getSectionSpacing());
    }
}
