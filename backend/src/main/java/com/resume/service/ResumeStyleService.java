package com.resume.service;

import com.resume.entity.ResumeStyle;
import com.resume.repository.ResumeStyleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ResumeStyleService {

    private final ResumeStyleRepository repository;

    public ResumeStyleService(ResumeStyleRepository repository) {
        this.repository = repository;
    }

    public Optional<ResumeStyle> getStyle(String resumeId, String themeId) {
        return repository.findByResumeIdAndThemeId(resumeId, themeId);
    }

    @Transactional
    public ResumeStyle saveStyle(String resumeId, String themeId, ResumeStyle incoming) {
        ResumeStyle style = repository.findByResumeIdAndThemeId(resumeId, themeId)
                .orElseGet(() -> {
                    ResumeStyle s = new ResumeStyle();
                    s.setResumeId(resumeId);
                    s.setThemeId(themeId);
                    return s;
                });
        if (incoming.getFontSize() != null) style.setFontSize(incoming.getFontSize());
        if (incoming.getLineHeight() != null) style.setLineHeight(incoming.getLineHeight());
        if (incoming.getSectionSpacing() != null) style.setSectionSpacing(incoming.getSectionSpacing());
        return repository.save(style);
    }
}
