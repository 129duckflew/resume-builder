package com.resume.service;

import com.resume.dto.*;
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

    // ─── Diff tests ──────────────────────────────────────────────────────

    @Test
    void diff_identicalContent_allUnchanged() {
        String content = "# Title\n\nSome body text.\n\nMore lines here.\n";
        ResumeVersion vA = createVersion(1, "Title", content);
        ResumeVersion vB = createVersion(2, "Title", content);

        when(repository.findByResumeIdAndVersionNumber("r1", 1)).thenReturn(Optional.of(vA));
        when(repository.findByResumeIdAndVersionNumber("r1", 2)).thenReturn(Optional.of(vB));

        VersionDiffResponse result = service.getDiff("r1", 1, 2);

        assertEquals(1, result.getVersionA().getVersionNumber());
        assertEquals(2, result.getVersionB().getVersionNumber());
        // When identical, no hunks (no changed regions)
        assertTrue(result.getHunks().isEmpty(), "Identical content should produce no hunks");
    }

    @Test
    void diff_allAdded_noOldContent() {
        String newContent = "# Brand New\n\nJust created.\n";
        ResumeVersion vA = createVersion(1, "Old", null);
        ResumeVersion vB = createVersion(2, "New", newContent);

        when(repository.findByResumeIdAndVersionNumber("r1", 1)).thenReturn(Optional.of(vA));
        when(repository.findByResumeIdAndVersionNumber("r1", 2)).thenReturn(Optional.of(vB));

        VersionDiffResponse result = service.getDiff("r1", 1, 2);

        for (Hunk hunk : result.getHunks()) {
            for (DiffLine line : hunk.getLines()) {
                assertEquals(LineType.ADDED, line.getType());
            }
        }
    }

    @Test
    void diff_allRemoved_noNewContent() {
        String oldContent = "# Old Stuff\n\nGone.\n";
        ResumeVersion vA = createVersion(1, "Old", oldContent);
        ResumeVersion vB = createVersion(2, "New", null);

        when(repository.findByResumeIdAndVersionNumber("r1", 1)).thenReturn(Optional.of(vA));
        when(repository.findByResumeIdAndVersionNumber("r1", 2)).thenReturn(Optional.of(vB));

        VersionDiffResponse result = service.getDiff("r1", 1, 2);

        for (Hunk hunk : result.getHunks()) {
            for (DiffLine line : hunk.getLines()) {
                assertEquals(LineType.REMOVED, line.getType());
            }
        }
    }

    @Test
    void diff_mixedChanges_correctHunks() {
        ResumeVersion vA = createVersion(1, "V1", "Line A\nLine B\nLine C\nLine D\nLine E\n");
        ResumeVersion vB = createVersion(2, "V2", "Line A\nLine X\nLine C\nLine D\nLine Y\n");

        when(repository.findByResumeIdAndVersionNumber("r1", 1)).thenReturn(Optional.of(vA));
        when(repository.findByResumeIdAndVersionNumber("r1", 2)).thenReturn(Optional.of(vB));

        VersionDiffResponse result = service.getDiff("r1", 1, 2);

        // Should have changes around lines 2 and 5
        assertFalse(result.getHunks().isEmpty());
    }

    @Test
    void diff_changedLine_asRemoveAddPair() {
        ResumeVersion vA = createVersion(1, "V1", "Keep\nOldLine\nKeep2\n");
        ResumeVersion vB = createVersion(2, "V2", "Keep\nNewLine\nKeep2\n");

        when(repository.findByResumeIdAndVersionNumber("r1", 1)).thenReturn(Optional.of(vA));
        when(repository.findByResumeIdAndVersionNumber("r1", 2)).thenReturn(Optional.of(vB));

        VersionDiffResponse result = service.getDiff("r1", 1, 2);

        // Find the hunk with changes
        boolean foundRemoveAdd = false;
        for (Hunk hunk : result.getHunks()) {
            for (DiffLine line : hunk.getLines()) {
                if (line.getType() == LineType.REMOVED && "OldLine".equals(line.getText())) {
                    foundRemoveAdd = true;
                }
            }
        }
        assertTrue(foundRemoveAdd, "Should have a REMOVED line with 'OldLine'");
    }

    @Test
    void diff_hunks_merge_whenGapLeq3() {
        // Changes close together (gap ≤ 3) should merge into a single hunk
        // Changes at lines 2 and 6, gap = 3 (lines 3-5 unchanged) → merge
        ResumeVersion vA = createVersion(1, "V1", "1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n");
        ResumeVersion vB = createVersion(2, "V2", "1\nX\n3\n4\n5\nY\n7\n8\n9\n10\n");

        when(repository.findByResumeIdAndVersionNumber("r1", 1)).thenReturn(Optional.of(vA));
        when(repository.findByResumeIdAndVersionNumber("r1", 2)).thenReturn(Optional.of(vB));

        VersionDiffResponse result = service.getDiff("r1", 1, 2);

        assertEquals(1, result.getHunks().size(), "Gap ≤ 3 should merge into 1 hunk");
    }

    @Test
    void diff_hunks_separate_whenGapGt3() {
        // Changes far apart (gap > 3) should be separate hunks
        // Changes at lines 2 and 7, gap = 4 (lines 3-6 unchanged) → separate
        ResumeVersion vA = createVersion(1, "V1", "1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n");
        ResumeVersion vB = createVersion(2, "V2", "1\nX\n3\n4\n5\n6\nY\n8\n9\n10\n");

        when(repository.findByResumeIdAndVersionNumber("r1", 1)).thenReturn(Optional.of(vA));
        when(repository.findByResumeIdAndVersionNumber("r1", 2)).thenReturn(Optional.of(vB));

        VersionDiffResponse result = service.getDiff("r1", 1, 2);

        assertEquals(2, result.getHunks().size(), "Gap > 3 should produce 2 separate hunks");
    }

    @Test
    void diff_hunkSeparate_gapGt3() {
        // Changes far apart (gap > 3) should be separate hunks
        ResumeVersion vA = createVersion(1, "V1", "A\nB\nC\nD\nE\nF\nG\nH\nI\nJ\n");
        ResumeVersion vB = createVersion(2, "V2", "X\nB\nC\nD\nE\nF\nY\nH\nI\nJ\n");

        when(repository.findByResumeIdAndVersionNumber("r1", 1)).thenReturn(Optional.of(vA));
        when(repository.findByResumeIdAndVersionNumber("r1", 2)).thenReturn(Optional.of(vB));

        VersionDiffResponse result = service.getDiff("r1", 1, 2);

        // Changes at line 1 and 7 with gap > 3 -> 2 hunks expected
        assertEquals(2, result.getHunks().size(), "Expected 2 separate hunks");
    }

    @Test
    void diff_singleLineReplace_correctHunkHeader() {
        // Single line replacement: verify hunk header line numbers
        ResumeVersion vA = createVersion(1, "V1", "# Old Title");
        ResumeVersion vB = createVersion(2, "V2", "# New Title");

        when(repository.findByResumeIdAndVersionNumber("r1", 1)).thenReturn(Optional.of(vA));
        when(repository.findByResumeIdAndVersionNumber("r1", 2)).thenReturn(Optional.of(vB));

        VersionDiffResponse result = service.getDiff("r1", 1, 2);

        assertEquals(1, result.getHunks().size());
        Hunk hunk = result.getHunks().get(0);
        assertEquals(1, hunk.getOldStart(), "oldStart should be 1");
        assertEquals(1, hunk.getOldCount(), "oldCount should be 1");
        assertEquals(1, hunk.getNewStart(), "newStart should be 1");
        assertEquals(1, hunk.getNewCount(), "newCount should be 1");

        List<DiffLine> lines = hunk.getLines();
        boolean foundRemoved = false, foundAdded = false;
        for (DiffLine dl : lines) {
            if (dl.getType() == LineType.REMOVED && "# Old Title".equals(dl.getText())) foundRemoved = true;
            if (dl.getType() == LineType.ADDED && "# New Title".equals(dl.getText())) foundAdded = true;
        }
        assertTrue(foundRemoved, "Should contain REMOVED '# Old Title'");
        assertTrue(foundAdded, "Should contain ADDED '# New Title'");
    }

    @Test
    void diff_versionNotFound_throws() {
        when(repository.findByResumeIdAndVersionNumber("r1", 99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getDiff("r1", 99, 1));
    }

    private ResumeVersion createVersion(int versionNumber, String title, String content) {
        ResumeVersion v = new ResumeVersion();
        v.setResumeId("r1");
        v.setVersionNumber(versionNumber);
        v.setTitle(title);
        v.setContent(content);
        return v;
    }
}
