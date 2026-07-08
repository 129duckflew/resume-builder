package com.resume.service;

import com.resume.dto.ResumeDTO;
import com.resume.entity.Resume;
import com.resume.repository.ResumeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;

    public ResumeService(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    public List<Resume> findByUserId(Long userId) {
        return resumeRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    public Optional<Resume> findByIdAndUserId(String id, Long userId) {
        return resumeRepository.findByIdAndUserId(id, userId);
    }

    public Resume create(ResumeDTO dto, Long userId) {
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setTitle(dto.getTitle());
        resume.setContent(dto.getContent() != null ? dto.getContent() : DEFAULT_CONTENT);
        resume.setThemeId(dto.getThemeId() != null ? dto.getThemeId() : "classic");
        resume.setFontSize(dto.getFontSize());
        resume.setLineHeight(dto.getLineHeight());
        resume.setSectionSpacing(dto.getSectionSpacing() != null ? dto.getSectionSpacing() : "normal");
        return resumeRepository.save(resume);
    }

    private static final String DEFAULT_CONTENT = """
# YOUR NAME
## Job Title / Tagline

Email: your.email@example.com | Tel: +1 (555) 123-4567 | LinkedIn: linkedin.com/in/yourname

## Summary

Write a brief 2-3 sentence professional summary that highlights your key qualifications and career objectives.

## Experience

### Company Name | Location | Dates
**Job Title**
- Key achievement or responsibility
- Another accomplishment with measurable results
- Additional notable contribution

### Previous Company | Location | Dates
**Previous Role**
- Key achievement or responsibility
- Another accomplishment

## Education

### Degree Name | University | Year
- Major / GPA / Honors (optional)
- Relevant coursework or achievements

## Skills

- **Technical:** Skill 1, Skill 2, Skill 3
- **Languages:** Language 1, Language 2
- **Certifications:** Cert 1, Cert 2
""";

    public Resume update(String id, ResumeDTO dto, Long userId) {
        Resume resume = resumeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Resume not found: " + id));
        if (dto.getTitle() != null) resume.setTitle(dto.getTitle());
        if (dto.getContent() != null) resume.setContent(dto.getContent());
        if (dto.getThemeId() != null) resume.setThemeId(dto.getThemeId());
        if (dto.getFontSize() != null) resume.setFontSize(dto.getFontSize());
        if (dto.getLineHeight() != null) resume.setLineHeight(dto.getLineHeight());
        if (dto.getSectionSpacing() != null) resume.setSectionSpacing(dto.getSectionSpacing());
        return resumeRepository.save(resume);
    }

    public void delete(String id, Long userId) {
        Resume resume = resumeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Resume not found: " + id));
        resumeRepository.delete(resume);
    }

    @Transactional
    public void assignOrphanResumes(Long userId) {
        List<Resume> orphans = resumeRepository.findByUserIdIsNull();
        for (Resume r : orphans) {
            r.setUserId(userId);
            resumeRepository.save(r);
        }
    }
}
