package com.resume.mcp;

import com.resume.dto.ResumeDTO;
import com.resume.dto.VersionDiffResponse;
import com.resume.entity.Resume;
import com.resume.entity.ResumeVersion;
import com.resume.service.ResumeService;
import com.resume.service.ResumeVersionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class McpResumeToolsTest {

    @Mock
    private ResumeService resumeService;
    @Mock
    private ResumeVersionService versionService;

    private McpResumeTools tools;

    @BeforeEach
    void setUp() {
        tools = new McpResumeTools(resumeService, versionService);
    }

    @Test
    void listResumes_withUserId_shouldDelegateToFindByUserId() {
        when(resumeService.findByUserId(1L)).thenReturn(List.of(new Resume()));

        List<Resume> result = tools.listResumes(1L);

        assertThat(result).hasSize(1);
        verify(resumeService).findByUserId(1L);
        verifyNoMoreInteractions(resumeService);
    }

    @Test
    void listResumes_withoutUserId_shouldDelegateToFindAll() {
        when(resumeService.findAll()).thenReturn(List.of(new Resume(), new Resume()));

        List<Resume> result = tools.listResumes(null);

        assertThat(result).hasSize(2);
        verify(resumeService).findAll();
    }

    @Test
    void getResume_shouldReturnResume() {
        Resume resume = new Resume();
        resume.setId("abc-123");
        when(resumeService.findById("abc-123")).thenReturn(Optional.of(resume));

        Resume result = tools.getResume("abc-123");

        assertThat(result.getId()).isEqualTo("abc-123");
    }

    @Test
    void getResume_notFound_shouldThrow() {
        when(resumeService.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tools.getResume("missing"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void createResume_shouldDelegateToService() {
        Resume expected = new Resume();
        expected.setId("new-id");
        when(resumeService.create(any(ResumeDTO.class), eq(1L))).thenReturn(expected);

        Resume result = tools.createResume(1L, "My Resume", "# Content", "classic");

        assertThat(result.getId()).isEqualTo("new-id");
        verify(resumeService).create(any(ResumeDTO.class), eq(1L));
    }

    @Test
    void updateResume_shouldDelegateToUpdateDirect() {
        Resume expected = new Resume();
        expected.setId("abc");
        when(resumeService.updateDirect(eq("abc"), any(ResumeDTO.class))).thenReturn(expected);

        Resume result = tools.updateResume("abc", "New Title", null, null, null, null, null);

        assertThat(result.getId()).isEqualTo("abc");
        verify(resumeService).updateDirect(eq("abc"), any(ResumeDTO.class));
    }

    @Test
    void deleteResume_shouldDelegateToDeleteDirect() {
        tools.deleteResume("abc");

        verify(resumeService).deleteDirect("abc");
    }

    @Test
    void listVersions_shouldDelegateToService() {
        when(versionService.getVersions("r1")).thenReturn(List.of(new ResumeVersion()));

        List<ResumeVersion> result = tools.listVersions("r1");

        assertThat(result).hasSize(1);
    }

    @Test
    void getVersion_shouldDelegateToService() {
        ResumeVersion v = new ResumeVersion();
        v.setVersionNumber(3);
        when(versionService.getVersion("r1", 3)).thenReturn(v);

        ResumeVersion result = tools.getVersion("r1", 3);

        assertThat(result.getVersionNumber()).isEqualTo(3);
    }

    @Test
    void restoreVersion_shouldCallRestoreFromVersionDirect() {
        Resume restored = new Resume();
        restored.setId("r1");
        when(versionService.restoreVersion("r1", 2)).thenReturn(restored);
        when(resumeService.restoreFromVersionDirect(restored)).thenReturn(restored);

        Resume result = tools.restoreVersion("r1", 2);

        assertThat(result.getId()).isEqualTo("r1");
    }

    @Test
    void diffVersions_shouldDelegateToService() {
        VersionDiffResponse expected = new VersionDiffResponse(null, null, List.of());
        when(versionService.getDiff("r1", 1, 2)).thenReturn(expected);

        VersionDiffResponse result = tools.diffVersions("r1", 1, 2);

        assertThat(result).isSameAs(expected);
    }
}
