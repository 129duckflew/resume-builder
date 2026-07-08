package com.resume.service;

import com.resume.entity.Resume;
import com.resume.entity.ResumeVersion;
import com.resume.repository.ResumeVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
