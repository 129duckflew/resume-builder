package com.resume.service;

import com.resume.dto.ResumeDTO;
import com.resume.entity.Resume;
import com.resume.repository.ResumeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;

    public ResumeService(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    public List<Resume> findAll() {
        return resumeRepository.findAllByOrderByUpdatedAtDesc();
    }

    public Optional<Resume> findById(String id) {
        return resumeRepository.findById(id);
    }

    public Resume create(ResumeDTO dto) {
        Resume resume = new Resume();
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

    public Resume update(String id, ResumeDTO dto) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found: " + id));
        resume.setTitle(dto.getTitle());
        resume.setContent(dto.getContent());
        resume.setThemeId(dto.getThemeId());
        resume.setFontSize(dto.getFontSize());
        resume.setLineHeight(dto.getLineHeight());
        resume.setSectionSpacing(dto.getSectionSpacing());
        return resumeRepository.save(resume);
    }

    public void delete(String id) {
        resumeRepository.deleteById(id);
    }
}
