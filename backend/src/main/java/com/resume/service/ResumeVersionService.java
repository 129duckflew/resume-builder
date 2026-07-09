package com.resume.service;

import com.resume.dto.*;
import com.resume.entity.Resume;
import com.resume.entity.ResumeVersion;
import com.resume.repository.ResumeVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ResumeVersionService {

    private static final int MAX_VERSIONS = 50;

    private final ResumeVersionRepository repository;

    public ResumeVersionService(ResumeVersionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void saveSnapshot(Resume resume) {
        ResumeVersion version = new ResumeVersion();
        version.setResumeId(resume.getId());
        version.setTitle(resume.getTitle());
        version.setContent(resume.getContent());
        version.setThemeId(resume.getThemeId());
        version.setFontSize(resume.getFontSize());
        version.setLineHeight(resume.getLineHeight());
        version.setSectionSpacing(resume.getSectionSpacing());

        Integer nextVersion = repository.findTopByResumeIdOrderByVersionNumberDesc(resume.getId())
                .map(v -> v.getVersionNumber() + 1)
                .orElse(1);
        version.setVersionNumber(nextVersion);

        repository.save(version);

        // Enforce version limit
        List<ResumeVersion> all = repository.findByResumeIdOrderByVersionNumberDesc(resume.getId());
        if (all.size() > MAX_VERSIONS) {
            for (int i = MAX_VERSIONS; i < all.size(); i++) {
                repository.delete(all.get(i));
            }
        }
    }

    public List<ResumeVersion> getVersions(String resumeId) {
        return repository.findByResumeIdOrderByVersionNumberDesc(resumeId);
    }

    public ResumeVersion getVersion(String resumeId, int versionNumber) {
        return repository.findByResumeIdAndVersionNumber(resumeId, versionNumber)
                .orElseThrow(() -> new RuntimeException("Version not found: " + versionNumber));
    }

    @Transactional
    public Resume restoreVersion(String resumeId, int versionNumber) {
        ResumeVersion version = getVersion(resumeId, versionNumber);
        Resume resume = new Resume();
        resume.setId(resumeId);
        resume.setTitle(version.getTitle());
        resume.setContent(version.getContent());
        resume.setThemeId(version.getThemeId());
        resume.setFontSize(version.getFontSize());
        resume.setLineHeight(version.getLineHeight());
        resume.setSectionSpacing(version.getSectionSpacing());
        return resume;
    }

    public VersionDiffResponse getDiff(String resumeId, int versionA, int versionB) {
        ResumeVersion vA = getVersion(resumeId, versionA);
        ResumeVersion vB = getVersion(resumeId, versionB);

        String[] oldLines = vA.getContent() != null ? vA.getContent().replace("\r\n", "\n").split("\n", -1) : new String[0];
        String[] newLines = vB.getContent() != null ? vB.getContent().replace("\r\n", "\n").split("\n", -1) : new String[0];

        List<DiffLine> diffs = computeDiff(oldLines, newLines);
        List<Hunk> hunks = assembleHunks(diffs);

        VersionMeta metaA = new VersionMeta(vA.getVersionNumber(), vA.getTitle(), vA.getCreatedAt());
        VersionMeta metaB = new VersionMeta(vB.getVersionNumber(), vB.getTitle(), vB.getCreatedAt());

        return new VersionDiffResponse(metaA, metaB, hunks);
    }

    private List<DiffLine> computeDiff(String[] oldLines, String[] newLines) {
        int m = oldLines.length;
        int n = newLines.length;

        // LCS matrix
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (oldLines[i - 1].equals(newLines[j - 1])) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        // Backtrack to produce operation list
        List<DiffLine> result = new ArrayList<>();
        int i = m, j = n;
        List<DiffLine> reversed = new ArrayList<>();
        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && oldLines[i - 1].equals(newLines[j - 1])) {
                reversed.add(new DiffLine(LineType.UNCHANGED, oldLines[i - 1]));
                i--;
                j--;
            } else if (j > 0 && (i == 0 || dp[i][j - 1] > dp[i - 1][j])) {
                reversed.add(new DiffLine(LineType.ADDED, newLines[j - 1]));
                j--;
            } else {
                reversed.add(new DiffLine(LineType.REMOVED, oldLines[i - 1]));
                i--;
            }
        }

        // Reverse to get chronological order
        for (int k = reversed.size() - 1; k >= 0; k--) {
            result.add(reversed.get(k));
        }

        return result;
    }

    private List<Hunk> assembleHunks(List<DiffLine> diffs) {
        // Walk through diffs and collect change regions.
        // Track old/new line numbers as we go.
        List<int[]> rawHunks = new ArrayList<>(); // each: {startIdx, endIdx(exclusive), oldStart, newStart}
        int idx = 0;
        int oldLine = 1, newLine = 1;
        while (idx < diffs.size()) {
            if (diffs.get(idx).getType() == LineType.UNCHANGED) {
                oldLine++;
                newLine++;
                idx++;
                continue;
            }
            int startIdx = idx;
            int oldStart = oldLine;
            int newStart = newLine;
            while (idx < diffs.size() && diffs.get(idx).getType() != LineType.UNCHANGED) {
                if (diffs.get(idx).getType() == LineType.ADDED) newLine++;
                else oldLine++;
                idx++;
            }
            rawHunks.add(new int[]{startIdx, idx, oldStart, newStart});
        }

        if (rawHunks.isEmpty()) return List.of();

        // Track old line positions for each raw hunk end
        // Compute oldEnd for merge decision
        List<int[]> merged = new ArrayList<>();
        merged.add(rawHunks.get(0));
        for (int r = 1; r < rawHunks.size(); r++) {
            int[] prev = merged.get(merged.size() - 1);
            int[] curr = rawHunks.get(r);

            // prev's old-end requires counting old-lines in prev
            int prevOldEnd = prev[2]; // starting old line of prev
            for (int i = prev[0]; i < prev[1]; i++) {
                if (diffs.get(i).getType() != LineType.ADDED) prevOldEnd++;
            }

            int gap = curr[2] - prevOldEnd;
            if (gap <= 3) {
                // merge: extend prev to cover curr
                merged.set(merged.size() - 1, new int[]{prev[0], curr[1], prev[2], prev[3]});
            } else {
                merged.add(curr);
            }
        }

        // Build Hunk objects with context
        List<Hunk> hunks = new ArrayList<>();
        for (int[] region : merged) {
            int ctxStart = Math.max(0, region[0] - 3);
            int ctxEnd = Math.min(diffs.size(), region[1] + 3);

            // Compute hunk old/new start by scanning from beginning
            int hunkOldStart = 1, hunkNewStart = 1;
            for (int i = 0; i < ctxStart; i++) {
                if (diffs.get(i).getType() == LineType.ADDED) {
                    hunkNewStart++;
                } else if (diffs.get(i).getType() == LineType.REMOVED) {
                    hunkOldStart++;
                } else { // UNCHANGED
                    hunkOldStart++;
                    hunkNewStart++;
                }
            }

            List<DiffLine> hunkLines = new ArrayList<>();
            for (int i = ctxStart; i < ctxEnd; i++) {
                hunkLines.add(diffs.get(i));
            }

            int oldCount = 0, newCount = 0;
            for (DiffLine dl : hunkLines) {
                if (dl.getType() == LineType.ADDED) newCount++;
                else if (dl.getType() == LineType.REMOVED) oldCount++;
                else { oldCount++; newCount++; }
            }

            hunks.add(new Hunk(hunkOldStart, oldCount, hunkNewStart, newCount, hunkLines));
        }

        return hunks;
    }
}
